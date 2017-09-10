package  dev.yn.playground.user

import dev.yn.playground.sql.*
import io.kotlintest.Spec
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.PostgreSQLClient
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

    val asyncPSQLConfig = JsonObject()
            .put("host", "localhost")
            .put("port", 5432)
            .put("database", "chitchat")
            .put("username", "chatty_tammy")
            .put("password", "gossipy")
            .put("maxPoolSize", 30)
    val vertx: Vertx by lazy {
        Vertx.vertx()
    }
    val executor: SQLTransactionExecutor by lazy {
        SQLTransactionExecutor(PostgreSQLClient.createShared(vertx, asyncPSQLConfig))
    }
    
    override protected fun interceptSpec(context: Spec, spec: () -> Unit) {

        val setUpFuture = executor.execute(Unit,
                SQLTransaction.exec(createUserProfileTable)
                        .exec(createUserPasswordTable)
                        .exec("CREATE EXTENSION IF NOT EXISTS pgcrypto"))

        while(!setUpFuture.isComplete) {
            println("setting up")
            Thread.sleep(500)
        }
        setUpFuture.succeeded() shouldBe true

        spec()

        val cleanupFuture = executor.execute(Unit,
                SQLTransaction.deleteAll("user_password")
                        .deleteAll("user_profile")
        ).compose {
            executor.execute(
                    Unit,
                    SQLTransaction.dropTable("user_password")
                            .dropTable("user_profile")
            )
        }
        while(!cleanupFuture.isComplete) {
            println("cleaning up")
            Thread.sleep(500)
        }

        cleanupFuture.succeeded() shouldBe true
    }

    init {
        "Create the user password repository" {
            val future = executor.update(UserAndPassword(user, password), UserTransactions.createUserTransaction)

            while(!future.isComplete) {
                println("waiting for future to complete..")
                Thread.sleep(500)
            }
            future.succeeded() shouldBe true

            val getFuture = executor.query(userAndPassword, UserTransactions.authenticateUserTransaction)
            while(!getFuture.isComplete) {
                println("waiting for future to complete..")
                Thread.sleep(500)
            }
            getFuture.succeeded() shouldBe true
            getFuture.result() shouldBe userId


            val authenticateBadPassword = executor.query(userAndPassword.copy(password = "bad"), UserTransactions.authenticateUserTransaction)
            while(!authenticateBadPassword.isComplete) {
                println("waiting for future to complete..")
                Thread.sleep(500)
            }
            authenticateBadPassword.failed() shouldBe true
            authenticateBadPassword.cause() shouldBe UserError.AuthenticationFailed
        }
    }




}