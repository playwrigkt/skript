package playwright.skript.chatroom

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import playwright.skript.common.ApplicationVenue
import playwright.skript.result.VertxResult
import playwright.skript.venue.VertxPublishVenue
import playwright.skript.venue.VertxSQLVenue
import playwright.skript.venue.VertxSerializeVenue

class VertxChatroomTransactionSpec: ChatroomTransactionsSpec() {

    companion object {
        val hikariConfig = JsonObject()
                .put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider")
                .put("jdbcUrl", "jdbc:postgresql://localhost:5432/chitchat")
                .put("username", "chatty_tammy")
                .put("password", "gossipy")
                .put("driver_class", "org.postgresql.Driver")
                .put("maximumPoolSize", 30)
                .put("poolName", "test_pool")

        val vertx by lazy { Vertx.vertx() }

        val sqlClient: SQLClient by lazy {
            JDBCClient.createShared(vertx, hikariConfig, "test_ds")
        }
        val sqlVenue = VertxSQLVenue(sqlClient)
        val publishVenue = VertxPublishVenue(vertx)
        val serializeVenue = VertxSerializeVenue()

        val provider: ApplicationVenue by lazy {
            ApplicationVenue(publishVenue, sqlVenue, serializeVenue)
        }
    }

    override fun provider(): ApplicationVenue = VertxChatroomTransactionSpec.provider

    override fun closeResources() {
        val clientF = Future.future<Void>()
        sqlClient.close(clientF.completer())
        awaitSucceededFuture(VertxResult(clientF))
        val future = Future.future<Void>()
        vertx.close(future.completer())
        awaitSucceededFuture(VertxResult(future))

    }
}