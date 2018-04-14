package  playwrigkt.skript.user

import io.kotlintest.*
import io.kotlintest.specs.StringSpec
import org.slf4j.LoggerFactory
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.deserialize
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.extensions.schema.dropUserSchema
import playwrigkt.skript.user.extensions.schema.initUserSchema
import playwrigkt.skript.user.extensions.transaction.deleteAllUsers
import playwrigkt.skript.user.models.*
import playwrigkt.skript.venue.QueueVenue
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit


abstract class UserServiceSpec : StringSpec() {

    val LOG = LoggerFactory.getLogger(this.javaClass)

    abstract fun stageManager(): ApplicationStageManager
    val userService: UserService by lazy {
        UserService(stageManager())
    }

    abstract fun closeResources()

    fun loginConsumer(): Produktion {
        return awaitSucceededFuture(
                userLoginProduktion(
                        queueVenue(),
                        stageManager(),
                        Skript.identity<QueueMessage, ApplicationTroupe<Unit>>()
                                .map { it.body }
                                .deserialize(UserSession::class.java)
                                .map(processedLoginEvents::add)
                                .map { Unit }))!!
    }

    val processedLoginEvents = LinkedBlockingQueue<UserSession>()

    fun createConsumer(): Produktion {
        return awaitSucceededFuture(
                userCreateProduktion(
                        queueVenue(),
                        stageManager(),
                        Skript.identity<QueueMessage, ApplicationTroupe<Unit>>()
                                .map { it.body }
                                .deserialize(UserProfile::class.java)
                                .map(processedCreateEvents::add)
                                .map { Unit }))!!
    }

    val processedCreateEvents = LinkedBlockingQueue<UserProfile>()

    abstract fun queueVenue(): QueueVenue

    override fun beforeSpec(description: Description, spec: Spec) {
        awaitSucceededFuture(stageManager().hireTroupe().dropUserSchema())
        awaitSucceededFuture(stageManager().hireTroupe().initUserSchema())
    }

    override fun afterSpec(description: Description, spec: Spec) {
        awaitSucceededFuture(stageManager().hireTroupe().deleteAllUsers())
        awaitSucceededFuture(stageManager().hireTroupe().dropUserSchema())
        closeResources()
    }

    init {
        "Login a userName" {
            val userId = UUID.randomUUID().toString()
            val password = "pass1"
            val userName = "sally"
            val user = UserProfile(userId, userName, false)
            val userAndPassword = UserNameAndPassword(userName, password)
            val createStream = createConsumer()
            val loginStream= loginConsumer()

            createStream.isRunning() shouldBe true
            loginStream.isRunning() shouldBe true

            awaitSucceededFuture(
                    userService.createUser(UserProfileAndPassword(user, password)),
                    user)

            val session = awaitSucceededFuture(userService.loginUser(userAndPassword))
            session?.userId shouldBe userId
            session shouldNotBe null

            awaitStreamItem(processedCreateEvents, user)
            awaitStreamItem(processedLoginEvents, session!!)
            awaitSucceededFuture(createStream.stop())
            awaitSucceededFuture(loginStream.stop())
        }

        "Fail to loginActionChain a user with a bad password" {
            val userId = UUID.randomUUID().toString()
            val password = "pass2"
            val userName = "sally2"
            val user = UserProfile(userId, userName, false)
            val userAndPassword = UserNameAndPassword(userName, password)

            awaitSucceededFuture(
                    userService.createUser(UserProfileAndPassword(user, password)),
                    user)

            val expectedError = UserError.AuthenticationFailed
            awaitFailedFuture(
                    userService.loginUser(userAndPassword.copy(password = "bad")),
                    expectedError)
        }

        "Fail to loginActionChain a user who is already logged in" {
            val userId = UUID.randomUUID().toString()
            val password = "pass3"
            val userName = "sally3"
            val user = UserProfile(userId, userName, false)
            val userAndPassword = UserNameAndPassword(userName, password)

            awaitSucceededFuture(
                    userService.createUser(UserProfileAndPassword(user, password)),
                    user)

            awaitSucceededFuture(userService.loginUser(userAndPassword))?.userId shouldBe userId
            val expectedError = UserError.SessionAlreadyExists(userId)

            awaitFailedFuture(
                    userService.loginUser(userAndPassword.copy(password = password)),
                    expectedError)
        }

        "Allow a user to get themselves once logged in" {
            val userId = UUID.randomUUID().toString()
            val password = "pass4"
            val userName = "sally4"
            val user = UserProfile(userId, userName, false)
            val userAndPassword = UserNameAndPassword(userName, password)

            awaitSucceededFuture(
                    userService.createUser(UserProfileAndPassword(user, password)),
                    user)

            val session = awaitSucceededFuture(userService.loginUser(userAndPassword))!!

            awaitSucceededFuture(
                    userService.getUser(userId, session.sessionKey),
                    user)
        }

        "Not Allow a user to select another user once logged in" {
            val userId = UUID.randomUUID().toString()
            val password = "pass5"
            val userName = "sally5"
            val user = UserProfile(userId, userName, false)
            val userAndPassword = UserNameAndPassword(userName, password)

            val userId2 = UUID.randomUUID().toString()
            val password2 = "pass6"
            val userName2 = "sally6"
            val user2 = UserProfile(userId2, userName2, false)

            awaitSucceededFuture(
                    userService.createUser(UserProfileAndPassword(user, password)),
                    user)
            awaitSucceededFuture(
                    userService.createUser(UserProfileAndPassword(user2, password2)),
                    user2)

            val session = awaitSucceededFuture(userService.loginUser(userAndPassword))!!

            awaitFailedFuture(
                    userService.getUser(userId2, session.sessionKey),
                    UserError.AuthorizationFailed)
        }

        "Not Allow a user with a bogus key to select another user" {
            val userId = UUID.randomUUID().toString()
            val password = "pass7"
            val userName = "sally7"
            val user = UserProfile(userId, userName, false)

            awaitSucceededFuture(
                    userService.createUser(UserProfileAndPassword(user, password)),
                    user)

            val sessionKey = UUID.randomUUID().toString()

            awaitFailedFuture(
                    userService.getUser(userId, sessionKey),
                    UserError.AuthenticationFailed)
        }
    }

    fun <T> awaitStreamItem(queue: BlockingQueue<T>, expected: T, maxDuration: Long = 1000L) {
        queue.poll(maxDuration, TimeUnit.MILLISECONDS) shouldBe expected
    }

    fun <T> awaitSucceededFuture(future: AsyncResult<T>, result: T? = null, maxDuration: Long = 1000L): T? {
        val start = System.currentTimeMillis()
        while(!future.isComplete() && System.currentTimeMillis() - start < maxDuration) {
            Thread.sleep(100)
        }
        if(!future.isComplete()) fail("Timeout")
        if(future.isFailure()) fail("Expected Success, ${future.error()}")
        future.isSuccess() shouldBe true
        result?.let { future.result() shouldBe it }
        return future.result()
    }

    fun <T> awaitFailedFuture(future: AsyncResult<T>, cause: Throwable? = null, maxDuration: Long = 1000L): Throwable? {
        val start = System.currentTimeMillis()
        while(!future.isComplete() && System.currentTimeMillis() - start < maxDuration) {
            Thread.sleep(100)
        }
        future.isFailure() shouldBe true
        cause?.let { future.error() shouldBe it}
        return future.error()
    }



}