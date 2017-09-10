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

class UserRepositorySpec: StringSpec() {
    val userId = UUID.randomUUID().toString()
    val password = "pass1"
    val userName = "sally"
    val user = User(userId, userName, false)
    val userAndPassword = UserNameAndPassword(userName, password)


    val createUserProfileTable = """
CREATE TABLE IF NOT EXISTS user_profile (
    id text PRIMARY KEY,
    name text UNIQUE,
    allow_public_message boolean
);"""

    val createUserPasswordTable = """CREATE TABLE IF NOT EXISTS user_password (
    user_id text REFERENCES user_profile(id) PRIMARY KEY,
    pswhash text
);
"""
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

        val setUpFuture = executor.execute(
                SQLTransaction.exec(createUserProfileTable)
                        .exec(createUserPasswordTable)
                        .exec("CREATE EXTENSION IF NOT EXISTS pgcrypto"))

        awaitFuture(setUpFuture)
        setUpFuture.succeeded() shouldBe true

        spec()

        val cleanupFuture = executor.execute(SQLTransaction
                .deleteAll("user_password")
                .deleteAll("user_profile")
        ).compose { executor.execute(SQLTransaction
                .dropTable("user_password")
                .dropTable("user_profile"))
        }
        awaitFuture(cleanupFuture)
        cleanupFuture.succeeded() shouldBe true
    }

    init {
        "Create the user password repository" {
            val future = executor.update(UserAndPassword(user, password), UserTransactions.createUserTransaction)

            awaitFuture(future)
            future.succeeded() shouldBe true

            val getFuture = executor.query(userAndPassword, UserTransactions.authenticateUserTransaction)
            awaitFuture(getFuture)
            getFuture.succeeded() shouldBe true
            getFuture.result() shouldBe userId


            val authenticateBadPassword = executor.query(userAndPassword.copy(password = "bad"), UserTransactions.authenticateUserTransaction)
            awaitFuture(authenticateBadPassword)
            authenticateBadPassword.failed() shouldBe true
            authenticateBadPassword.cause() shouldBe SQLError.OnStatement(SQLStatement.Parameterized(SelectUserByPassword.selectUserPassword, JsonArray(listOf(userId, "bad"))), UserError.AuthenticationFailed)
        }
    }

    fun <T> awaitFuture(future: Future<T>, maxDuration: Long = 1000L) {
        val start = System.currentTimeMillis()
        while(!future.isComplete && System.currentTimeMillis() - start < maxDuration) {
            Thread.sleep(100)
        }
    }




}