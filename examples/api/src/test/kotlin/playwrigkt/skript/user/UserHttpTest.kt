package playwrigkt.skript.user

import io.kotlintest.Description
import io.kotlintest.Spec
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import playwrigkt.skript.Async
import playwrigkt.skript.auth.TokenAndInput
import playwrigkt.skript.result.VertxResult
import playwrigkt.skript.stagemanager.*
import playwrigkt.skript.user.extensions.schema.dropUserSchema
import playwrigkt.skript.user.extensions.schema.initUserSchema
import playwrigkt.skript.user.extensions.transaction.deleteAllUsers
import playwrigkt.skript.user.models.UserNameAndPassword
import playwrigkt.skript.user.models.UserProfile
import playwrigkt.skript.user.models.UserProfileAndPassword
import playwrigkt.skript.venue.VertxHttpServerVenue
import playwrigkt.skript.venue.VertxVenue
import playwrigkt.skript.venue.userProduktions
import java.util.*
import kotlin.math.floor

class UserHttpTest: StringSpec() {
    companion object {
        val vertx by lazy { Vertx.vertx() }

        val hikariConfig = JsonObject()
                .put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider")
                .put("jdbcUrl", "jdbc:postgresql://localhost:5432/chitchat")
                .put("username", "chatty_tammy")
                .put("password", "gossipy")
                .put("driver_class", "org.postgresql.Driver")
                .put("maximumPoolSize", 30)
                .put("poolName", "test_pool")
        val sqlClient: SQLClient by lazy {
            JDBCClient.createShared(vertx, hikariConfig, "test_ds")
        }

        val sqlConnectionStageManager by lazy { VertxSQLStageManager(sqlClient) }
        val publishStageManager by lazy { VertxPublishStageManager(vertx)  }
        val serializeStageManager by lazy { VertxSerializeStageManager() }
        val httpStageMager by lazy { VertxHttpRequestStageManager(HttpClientOptions().setDefaultPort(port), vertx) }
        val stageManager: ApplicationStageManager by lazy {
            ApplicationStageManager(publishStageManager, sqlConnectionStageManager, serializeStageManager, httpStageMager)
        }

        val vertxVenue by lazy { VertxVenue(vertx) }

        val port = floor((Math.random() * 8000)).toInt() + 2000

        val httpServerVenue: VertxHttpServerVenue by lazy { VertxHttpServerVenue(vertx.createHttpServer(HttpServerOptions().setPort(port))) }
        val produktions by lazy {
            val future = userProduktions(httpServerVenue, stageManager)
            while(!future.isComplete()) {
                Thread.sleep(100)
            }
            future.result()!!
        }
    }

    fun stageManager(): ApplicationStageManager = VertxUserServiceSpec.stageManager

    fun closeResources() {
        val clientF = Future.future<Void>()
        sqlClient.close(clientF.completer())
        Async.awaitSucceededFuture(VertxResult(clientF))
        val future = Future.future<Void>()
        vertx.close(future.completer())
        Async.awaitSucceededFuture(VertxResult(future))
        produktions.forEach { Async.awaitSucceededFuture(it.stop()) }
    }

    override fun beforeSpec(description: Description, spec: Spec) {
        Async.awaitSucceededFuture(stageManager().hireTroupe().dropUserSchema())
        Async.awaitSucceededFuture(stageManager().hireTroupe().initUserSchema())
        val waitForIt = produktions
    }

    override fun afterSpec(description: Description, spec: Spec) {
        Async.awaitSucceededFuture(stageManager().hireTroupe().deleteAllUsers())
        Async.awaitSucceededFuture(stageManager().hireTroupe().dropUserSchema())
        closeResources()
    }

    init {

        "get a user via http" {
            val userId = UUID.randomUUID().toString()
            val password = "pass10"
            val userName = "sally10"
            val user = UserProfile(userId, userName, false)
            val userAndPassword = UserNameAndPassword(userName, password)

            Async.awaitSucceededFuture(UserHttpClient.createUserRequestSkript.run(UserProfileAndPassword(user, password), stageManager.hireTroupe())) shouldBe user

            val session = Async.awaitSucceededFuture(UserHttpClient.loginRequestSkript.run(userAndPassword, stageManager.hireTroupe()))!!

            Async.awaitSucceededFuture(UserHttpClient.getUserRequestSkript.run(TokenAndInput(session.sessionKey, userId), stageManager.hireTroupe())) shouldBe user
        }
    }
}