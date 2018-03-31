package playwrigkt.skript.user

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import playwright.skript.consumer.alpha.QueueConsumerTroupe
import playwrigkt.skript.common.ApplicationVenue
import playwrigkt.skript.consumer.alpha.VertxConsumerTroupe
import playwrigkt.skript.result.VertxResult
import playwrigkt.skript.venue.VertxPublishVenue
import playwrigkt.skript.venue.VertxSQLVenue
import playwrigkt.skript.venue.VertxSerializeVenue

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

        val sqlConnectionProvider by lazy { VertxSQLVenue(sqlClient) }
        val publishVenue by lazy { VertxPublishVenue(vertx)  }
        val serializeVenue = VertxSerializeVenue()
        val provider: ApplicationVenue by lazy {
            ApplicationVenue(publishVenue, sqlConnectionProvider, serializeVenue)
        }

        val consumerPerformerProvider = VertxConsumerTroupe(vertx)
    }

    override fun provider(): ApplicationVenue = VertxUserServiceSpec.provider
    override fun consumerPerformerProvider(): QueueConsumerTroupe = consumerPerformerProvider
    override fun closeResources() {
        val clientF = Future.future<Void>()
        sqlClient.close(clientF.completer())
        awaitSucceededFuture(VertxResult(clientF))
        val future = Future.future<Void>()
        vertx.close(future.completer())
        awaitSucceededFuture(VertxResult(future))

    }
}