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
import dev.yn.playground.user.extensions.schema.*
import dev.yn.playground.user.extensions.transaction.*
import io.kotlintest.TestCaseContext
import io.kotlintest.matchers.fail
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

    val executor: SQLTransactionExecutor by lazy {
        SQLTransactionExecutor(JDBCClient.createNonShared(vertx, jdbcConfig))
    }

    override protected fun interceptTestCase(context: TestCaseContext, test: () -> Unit) {
        test()
    }

    override protected fun interceptSpec(context: Spec, spec: () -> Unit) {
        awaitSucceededFuture(executor.initUserSchema())
        spec()
        awaitSucceededFuture(executor.deleteAllUsers()
                .dropUserSchema(executor))
    }

    init {
        "Login a userName" {
            val userId = UUID.randomUUID().toString()
            val password = "pass1"
            val userName = "sally"
            val user = UserProfile(userId, userName, false)
            val userAndPassword = UserNameAndPassword(userName, password)

            val userService = UserService(executor)
            awaitSucceededFuture(
                    userService.createUser(UserProfileAndPassword(user, password)),
                    UserProfileAndPassword(user, password))

            awaitSucceededFuture(userService.loginUser(userAndPassword))
                    .let { it.userId shouldBe userId }
        }

        "Fail to login a user with a bad password" {
            val userId = UUID.randomUUID().toString()
            val password = "pass2"
            val userName = "sally2"
            val user = UserProfile(userId, userName, false)
            val userAndPassword = UserNameAndPassword(userName, password)

            val userService = UserService(executor)
            awaitSucceededFuture(
                    userService.createUser(UserProfileAndPassword(user, password)),
                    UserProfileAndPassword(user, password))

            val expectedError = SQLError.OnStatement(SQLStatement.Parameterized(ValidatePasswordForUserId.selectUserPassword, JsonArray(listOf(userId, "bad"))), UserError.AuthenticationFailed)
            awaitFailedFuture(
                    userService.loginUser(userAndPassword.copy(password = "bad")),
                    expectedError)
        }

        "Fail to login a user who is already logged in" {
            val userId = UUID.randomUUID().toString()
            val password = "pass3"
            val userName = "sally3"
            val user = UserProfile(userId, userName, false)
            val userAndPassword = UserNameAndPassword(userName, password)

            val userService = UserService(executor)
            awaitSucceededFuture(
                    userService.createUser(UserProfileAndPassword(user, password)),
                    UserProfileAndPassword(user, password))

            awaitSucceededFuture(userService.loginUser(userAndPassword))
                    .let { it.userId shouldBe userId }
            val expectedError = SQLError.OnStatement(SQLStatement.Parameterized(EnsureNoSessionExists.selectUserSessionExists, JsonArray(listOf(userId))), UserError.SessionAlreadyExists(userId))
            awaitFailedFuture(
                    userService.loginUser(userAndPassword.copy(password = password)),
                    expectedError)
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