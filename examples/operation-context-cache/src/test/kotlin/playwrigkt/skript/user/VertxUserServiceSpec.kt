package playwrigkt.skript.user

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import playwright.skript.venue.QueueVenue
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.result.VertxResult
import playwrigkt.skript.stagemanager.VertxPublishStageManager
import playwrigkt.skript.stagemanager.VertxSQLStageManager
import playwrigkt.skript.stagemanager.VertxSerializeStageManager
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

        val sqlConnectionProvider by lazy { VertxSQLStageManager(sqlClient) }
        val publishVenue by lazy { VertxPublishStageManager(vertx) }
        val serializeVenue by lazy { VertxSerializeStageManager() }

        val provider: ApplicationStageManager by lazy {
            ApplicationStageManager(publishVenue, sqlConnectionProvider, serializeVenue)
        }
        val CONSUMER_TROUPE: QueueVenue = VertxVenue(vertx)
    }

    override fun provider(): ApplicationStageManager = provider

    override fun consumerPerformerProvider(): QueueVenue = CONSUMER_TROUPE

    override fun closeResources() {
        val clientF = Future.future<Void>()
        sqlClient.close(clientF.completer())
        awaitSucceededFuture(VertxResult(clientF))
        val future = Future.future<Void>()
        vertx.close(future.completer())
        awaitSucceededFuture(VertxResult(future))

    }
}