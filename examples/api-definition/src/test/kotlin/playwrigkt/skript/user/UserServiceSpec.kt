package  playwrigkt.skript.user

import io.kotlintest.*
import io.kotlintest.specs.StringSpec
import playwrigkt.skript.Async
import playwrigkt.skript.Skript
import playwrigkt.skript.application.ApplicationRegistry
import playwrigkt.skript.application.HttpProduktionManagerLoader
import playwrigkt.skript.application.SkriptApplicationLoader
import playwrigkt.skript.application.createApplication
import playwrigkt.skript.auth.TokenAndInput
import playwrigkt.skript.ex.createFile
import playwrigkt.skript.ex.join
import playwrigkt.skript.ex.readFile
import playwrigkt.skript.ex.writeFile
import playwrigkt.skript.file.FileReference
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.produktion.ProduktionsManager
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.stagemanager.SyncJacksonSerializeStageManager
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.troupe.SyncFileTroupe
import playwrigkt.skript.user.extensions.schema.dropUserSchema
import playwrigkt.skript.user.extensions.schema.initUserSchema
import playwrigkt.skript.user.extensions.transaction.deleteAllUsers
import playwrigkt.skript.user.models.UserError
import playwrigkt.skript.user.models.UserNameAndPassword
import playwrigkt.skript.user.models.UserProfile
import playwrigkt.skript.user.models.UserProfileAndPassword
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.math.floor


abstract class UserServiceSpec : StringSpec() {
    abstract val sourceConfigFileName: String
    val port: Int = floor((Math.random() * 8000)).toInt() + 2000

    fun configFile() = sourceConfigFileName.split(".").joinToString("-$port.")

    val skriptApplication by lazy { Async.awaitSucceededFuture(createApplication(configFile()))!! }

    val stageManager by lazy  {
        skriptApplication.applicationResources
                .filter { it.value is ApplicationStageManager }
                .map { it.value as ApplicationStageManager }
                .first()
    }

    val httpProduktiionManager by lazy  {
        skriptApplication.applicationResources
                .get(HttpProduktionManagerLoader.name())
                ?.let { it as ProduktionsManager<HttpServer.Endpoint, HttpServer.Request<ByteArray>, HttpServer.Response, ApplicationTroupe> }!!
    }

    val userHttpClient = UserHttpClient()
    val userService: UserService by lazy { UserService(stageManager) }


    override fun beforeSpec(description: Description, spec: Spec) {
        val loader = SkriptApplicationLoader(SyncFileTroupe, SyncJacksonSerializeStageManager().hireTroupe(), ApplicationRegistry())
        Skript.identity<Unit, SkriptApplicationLoader>()
                .map { FileReference.Relative(sourceConfigFileName) }
                .readFile()
                .map { it.readText() }
                .map { it.replace("\"port\":8080", "\"port\":$port")}
                .split(Skript.identity<String, SkriptApplicationLoader>()
                        .map { FileReference.Relative(configFile()) }
                        .createFile()
                        .writeFile())
                .join { json, writer ->
                    writer.write(json)
                    writer.flush()
                    writer.close()
                }
                .run(Unit, loader)
        awaitSucceededFuture(stageManager.hireTroupe().dropUserSchema())
        awaitSucceededFuture(stageManager.hireTroupe().initUserSchema())
        awaitSucceededFuture(httpProduktiionManager.produktionManagers)
    }

    override fun afterSpec(description: Description, spec: Spec) {
        awaitSucceededFuture(stageManager.hireTroupe().deleteAllUsers())
        awaitSucceededFuture(stageManager.hireTroupe().dropUserSchema())
        awaitSucceededFuture(skriptApplication.tearDown())
        Files.delete(Paths.get(configFile()))
    }

    init {
        "Login a userName" {
            UserQueueSkripts.processedCreateEvents.clear()
            UserQueueSkripts.processedLoginEvents.clear()
            val userId = UUID.randomUUID().toString()
            val password = "pass1"
            val userName = "sally"
            val user = UserProfile(userId, userName, false)
            val userAndPassword = UserNameAndPassword(userName, password)

            awaitSucceededFuture(
                    userService.createUser(UserProfileAndPassword(user, password)),
                    user)

            val session = awaitSucceededFuture(userService.loginUser(userAndPassword))
            session?.userId shouldBe userId
            session shouldNotBe null

            awaitStreamItem(UserQueueSkripts.processedCreateEvents, user)
            awaitStreamItem(UserQueueSkripts.processedLoginEvents, session!!)
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
                    UserError.AuthenticationFailed)
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

        "get a user via http" {
            val userId = UUID.randomUUID().toString()
            val password = "pass10"
            val userName = "sally10"
            val user = UserProfile(userId, userName, false)
            val userAndPassword = UserNameAndPassword(userName, password)

            Async.awaitSucceededFuture(userHttpClient.createUserRequestSkript.run(UserProfileAndPassword(user, password), stageManager.hireTroupe())) shouldBe user

            val session = Async.awaitSucceededFuture(userHttpClient.loginRequestSkript.run(userAndPassword, stageManager.hireTroupe()))!!

            Async.awaitSucceededFuture(userHttpClient.getUserRequestSkript.run(TokenAndInput(session.sessionKey, userId), stageManager.hireTroupe())) shouldBe user
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