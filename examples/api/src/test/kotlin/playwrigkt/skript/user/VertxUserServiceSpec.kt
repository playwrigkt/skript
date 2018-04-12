package playwrigkt.skript.user

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import playwrigkt.skript.result.VertxResult
import playwrigkt.skript.stagemanager.*
import playwrigkt.skript.venue.QueueVenue
import playwrigkt.skript.venue.VertxVenue

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
        val sqlClient: SQLClient by lazy {
            JDBCClient.createShared(vertx, hikariConfig, "test_ds")
        }

        val sqlConnectionStageManager by lazy { VertxSQLStageManager(sqlClient) }
        val publishStageManager by lazy { VertxPublishStageManager(vertx)  }
        val serializeStageManager by lazy { VertxSerializeStageManager() }
        val httpStageManager by lazy { VertxHttpRequestStageManager(HttpClientOptions().setDefaultPort(UserHttpTest.port), UserHttpTest.vertx) }
        val stageManager: ApplicationStageManager by lazy {
            ApplicationStageManager(publishStageManager, sqlConnectionStageManager, serializeStageManager, httpStageManager)
        }

        val vertxVenue by lazy { VertxVenue(vertx) }
    }

    override fun stageManager(): ApplicationStageManager = VertxUserServiceSpec.stageManager
    override fun queueVenue(): QueueVenue = vertxVenue

    override fun closeResources() {
        val clientF = Future.future<Void>()
        sqlClient.close(clientF.completer())
        awaitSucceededFuture(VertxResult(clientF))
        val future = Future.future<Void>()
        vertx.close(future.completer())
        awaitSucceededFuture(VertxResult(future))
    }
}