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

class UserRepositorySpec: StringSpec() {
    val userId = UUID.randomUUID().toString()
    val password = "pass1"
    val userName = "sally"
    val user = User(userId, userName, false)
    val userAndPassword = UserNameAndPassword(userName, password)

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

    override protected fun interceptSpec(context: Spec, spec: () -> Unit) {
        awaitSucceededFuture(executor.initUserSchema())

        spec()

        awaitSucceededFuture(executor.deleteAllUsers()
                .dropUserSchema(executor))
    }

    init {
        "Create the user password repository" {
            val userService = UserService(executor)
            awaitSucceededFuture(
                    userService.createUser(UserAndPassword(user, password)),
                    UserAndPassword(user, password))

            awaitSucceededFuture(
                    userService.loginUser(userAndPassword),
                    userId)

            val expectedError = SQLError.OnStatement(SQLStatement.Parameterized(SelectUserByPassword.selectUserPassword, JsonArray(listOf(userId, "bad"))), UserError.AuthenticationFailed)
            awaitFailedFuture(
                    userService.loginUser(userAndPassword.copy(password = "bad")),
                    expectedError)
        }
    }

    fun <T> awaitSucceededFuture(future: Future<T>, result: T? = null, maxDuration: Long = 1000L) {
        val start = System.currentTimeMillis()
        while(!future.isComplete && System.currentTimeMillis() - start < maxDuration) {
            Thread.sleep(100)
        }
        future.succeeded() shouldBe true
        result?.let { future.result() shouldBe it }
    }

    fun <T> awaitFailedFuture(future: Future<T>, cause: Throwable? = null, maxDuration: Long = 1000L) {
        val start = System.currentTimeMillis()
        while(!future.isComplete && System.currentTimeMillis() - start < maxDuration) {
            Thread.sleep(100)
        }
        future.failed() shouldBe true
        cause?.let { future.cause() shouldBe it}
    }



}