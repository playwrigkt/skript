package  dev.yn.playground.user

import dev.yn.playground.sql.*
import io.kotlintest.Spec
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import java.util.*
import dev.yn.playground.user.extensions.schema.dropUserSchema
import dev.yn.playground.user.extensions.schema.initUserSchema
import dev.yn.playground.user.extensions.transaction.deleteAllUsers
import io.kotlintest.TestCaseContext
import io.kotlintest.matchers.fail
import io.vertx.ext.sql.SQLClient
import org.slf4j.LoggerFactory

class UserRepositorySpec: StringSpec() {

    val LOG = LoggerFactory.getLogger(this.javaClass)

    val jdbcConfig = JsonObject()
            .put("url", "jdbc:postgresql://localhost:5432/chitchat")
            .put("user", "chatty_tammy")
            .put("password", "gossipy")
            .put("driver_class", "org.postgresql.Driver")
            .put("max_pool_size", 30)

    val vertx: Vertx by lazy {
        Vertx.vertx()
    }

    val sqlClient: SQLClient by lazy {
        JDBCClient.createShared(vertx, jdbcConfig)
    }

    val provider: SQLAndVertxProvider by lazy {
        SQLAndVertxProvider(vertx, sqlClient)
    }
    override fun interceptTestCase(context: TestCaseContext, test: () -> Unit) {
        test()
    }

    override fun interceptSpec(context: Spec, spec: () -> Unit) {
        awaitSucceededFuture(provider.dropUserSchema())
        awaitSucceededFuture(provider.initUserSchema())
        spec()
        awaitSucceededFuture(provider.deleteAllUsers()
                .dropUserSchema(provider))
    }

    init {
        "Login a userName" {
            val userId = UUID.randomUUID().toString()
            val password = "pass1"
            val userName = "sally"
            val user = dev.yn.playground.user.UserProfile(userId, userName, false)
            val userAndPassword = dev.yn.playground.user.UserNameAndPassword(userName, password)

            val userService = try {
                dev.yn.playground.user.UserService(sqlClient, vertx)
            } catch(e: NoClassDefFoundError) {
                LOG.error("error: ", e)
                LOG.error("cause: ", e.cause)
                LOG.error(e.stackTrace.joinToString { "\n\t" })
                throw RuntimeException("wtf??")
            }
            awaitSucceededFuture(
                    userService.createUser(dev.yn.playground.user.UserProfileAndPassword(user, password)),
                    user)

            awaitSucceededFuture(userService.loginUser(userAndPassword)).userId shouldBe userId
        }

        "Fail to loginActionChain a user with a bad password" {
            val userId = UUID.randomUUID().toString()
            val password = "pass2"
            val userName = "sally2"
            val user = dev.yn.playground.user.UserProfile(userId, userName, false)
            val userAndPassword = dev.yn.playground.user.UserNameAndPassword(userName, password)

            val userService = dev.yn.playground.user.UserService(sqlClient, vertx)
            awaitSucceededFuture(
                    userService.createUser(dev.yn.playground.user.UserProfileAndPassword(user, password)),
                    user)

            val expectedError = SQLError.OnStatement(SQLStatement.Parameterized(dev.yn.playground.user.ValidatePasswordForUserId.selectUserPassword, JsonArray(listOf(userId, "bad"))), dev.yn.playground.user.UserError.AuthenticationFailed)
            awaitFailedFuture(
                    userService.loginUser(userAndPassword.copy(password = "bad")),
                    expectedError)
        }

        "Fail to loginActionChain a user who is already logged in" {
            val userId = UUID.randomUUID().toString()
            val password = "pass3"
            val userName = "sally3"
            val user = dev.yn.playground.user.UserProfile(userId, userName, false)
            val userAndPassword = dev.yn.playground.user.UserNameAndPassword(userName, password)

            val userService = dev.yn.playground.user.UserService(sqlClient, vertx)
            awaitSucceededFuture(
                    userService.createUser(dev.yn.playground.user.UserProfileAndPassword(user, password)),
                    user)

            awaitSucceededFuture(userService.loginUser(userAndPassword)).userId shouldBe userId
            val expectedError = SQLError.OnStatement(SQLStatement.Parameterized(dev.yn.playground.user.EnsureNoSessionExists.selectUserSessionExists, JsonArray(listOf(userId))), dev.yn.playground.user.UserError.SessionAlreadyExists(userId))
            awaitFailedFuture(
                    userService.loginUser(userAndPassword.copy(password = password)),
                    expectedError)
        }

        "Allow a user to get themselves once logged in" {
            val userId = UUID.randomUUID().toString()
            val password = "pass4"
            val userName = "sally4"
            val user = dev.yn.playground.user.UserProfile(userId, userName, false)
            val userAndPassword = dev.yn.playground.user.UserNameAndPassword(userName, password)

            val userService = dev.yn.playground.user.UserService(sqlClient, vertx)
            awaitSucceededFuture(
                    userService.createUser(dev.yn.playground.user.UserProfileAndPassword(user, password)),
                    user)

            val session = awaitSucceededFuture(userService.loginUser(userAndPassword))

            awaitSucceededFuture(
                    userService.getUser(userId, session.sessionKey),
                    user)
        }

        "Not Allow a user to select another user once logged in" {
            val userId = UUID.randomUUID().toString()
            val password = "pass5"
            val userName = "sally5"
            val user = dev.yn.playground.user.UserProfile(userId, userName, false)
            val userAndPassword = dev.yn.playground.user.UserNameAndPassword(userName, password)

            val userId2 = UUID.randomUUID().toString()
            val password2 = "pass6"
            val userName2 = "sally6"
            val user2 = dev.yn.playground.user.UserProfile(userId2, userName2, false)

            val userService = dev.yn.playground.user.UserService(sqlClient, vertx)
            awaitSucceededFuture(
                    userService.createUser(dev.yn.playground.user.UserProfileAndPassword(user, password)),
                    user)
            awaitSucceededFuture(
                    userService.createUser(dev.yn.playground.user.UserProfileAndPassword(user2, password2)),
                    user2)

            val session = awaitSucceededFuture(userService.loginUser(userAndPassword))

            awaitFailedFuture(
                    userService.getUser(userId2, session.sessionKey),
                    SQLError.OnStatement(SQLStatement.Parameterized(dev.yn.playground.user.UserSQL.selectSessionByKey, JsonArray(listOf(session.sessionKey))), dev.yn.playground.user.UserError.AuthorizationFailed))
        }

        "Not Allow a user with a bogus key to select another user" {
            val userId = UUID.randomUUID().toString()
            val password = "pass7"
            val userName = "sally7"
            val user = dev.yn.playground.user.UserProfile(userId, userName, false)

            val userService = dev.yn.playground.user.UserService(sqlClient, vertx)
            awaitSucceededFuture(
                    userService.createUser(dev.yn.playground.user.UserProfileAndPassword(user, password)),
                    user)

            val sessionKey = UUID.randomUUID().toString()

            awaitFailedFuture(
                    userService.getUser(userId, sessionKey),
                    SQLError.OnStatement(SQLStatement.Parameterized(dev.yn.playground.user.UserSQL.selectSessionByKey, JsonArray(listOf(sessionKey))), dev.yn.playground.user.UserError.AuthenticationFailed))
        }
    }

    fun <T> awaitSucceededFuture(future: Future<T>, result: T? = null, maxDuration: Long = 1000L): T {
        val start = System.currentTimeMillis()
        while(!future.isComplete && System.currentTimeMillis() - start < maxDuration) {
            Thread.sleep(100)
        }
        if(!future.isComplete) fail("Timeout")
        if(future.failed()) LOG.error("Expected Success", future.cause())
        future.succeeded() shouldBe true
        result?.let { future.result() shouldBe it }
        return future.result()
    }

    fun <T> awaitFailedFuture(future: Future<T>, cause: Throwable? = null, maxDuration: Long = 1000L): Throwable {
        val start = System.currentTimeMillis()
        while(!future.isComplete && System.currentTimeMillis() - start < maxDuration) {
            Thread.sleep(100)
        }
        future.failed() shouldBe true
        cause?.let { future.cause() shouldBe it}
        return future.cause()
    }



}