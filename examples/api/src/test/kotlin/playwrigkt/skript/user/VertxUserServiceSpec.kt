package playwrigkt.skript.user

import io.kotlintest.Description
import io.kotlintest.Spec
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import playwrigkt.skript.result.VertxResult
import playwrigkt.skript.stagemanager.*
import playwrigkt.skript.venue.*
import kotlin.math.floor

class VertxUserServiceSpec: UserServiceSpec() {
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

        val port = floor((Math.random() * 8000)).toInt() + 2000



        val vertxVenue by lazy { VertxVenue(vertx) }
        val httpServerVenue: VertxHttpServerVenue by lazy { VertxHttpServerVenue(vertx, HttpServerOptions().setPort(port)) }

        val sqlConnectionStageManager by lazy { VertxSQLStageManager(vertx, hikariConfig, "test_datasource") }
        val publishStageManager by lazy { VertxPublishStageManager(vertx.eventBus())  }
        val serializeStageManager by lazy { VertxSerializeStageManager() }
        val httpStageManager by lazy { VertxHttpRequestStageManager(HttpClientOptions().setDefaultPort(port), vertx) }

        val stageManager: ApplicationStageManager by lazy {
            ApplicationStageManager(publishStageManager, sqlConnectionStageManager, serializeStageManager, httpStageManager, httpServerVenue, vertxVenue)
        }

        val userHttpClient by lazy { UserHttpClient(port) }
    }

    override fun userHttpClient(): UserHttpClient = userHttpClient
    override fun stageManager(): ApplicationStageManager = VertxUserServiceSpec.stageManager

    override fun afterSpec(description: Description, spec: Spec) {
        super.afterSpec(description, spec)
        val future = Future.future<Void>()
        vertx.close(future.completer())
        awaitSucceededFuture(VertxResult(future))
    }
}