package playwrigkt.skript.chatroom

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import playwrigkt.skript.result.VertxResult
import playwrigkt.skript.stagemanager.*
import kotlin.math.floor

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
        val sqlConnectionStageManager by lazy { VertxSQLStageManager(sqlClient) }
        val publishStageManager by lazy { VertxPublishStageManager(vertx) }
        val serializeStageManager by lazy { VertxSerializeStageManager() }

        val port = floor((Math.random() * 8000)).toInt() + 2000
        val httpStageManager by lazy { VertxHttpRequestStageManager(HttpClientOptions().setDefaultPort(port), vertx) }
        val stageManager: ApplicationStageManager by lazy {
            ApplicationStageManager(publishStageManager, sqlConnectionStageManager, serializeStageManager, httpStageManager)
        }
    }

    override fun stageManager(): ApplicationStageManager = VertxChatroomTransactionSpec.stageManager

    override fun closeResources() {
        val clientF = Future.future<Void>()
        sqlClient.close(clientF.completer())
        awaitSucceededFuture(VertxResult(clientF))
        val future = Future.future<Void>()
        vertx.close(future.completer())
        awaitSucceededFuture(VertxResult(future))

    }
}