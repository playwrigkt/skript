package playwrigkt.skript.chatroom

import io.kotlintest.Description
import io.kotlintest.Spec
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import playwrigkt.skript.ExampleApplication
import playwrigkt.skript.result.VertxResult
import playwrigkt.skript.stagemanager.*
import playwrigkt.skript.venue.HttpServerVenue
import playwrigkt.skript.venue.QueueVenue
import playwrigkt.skript.venue.VertxHttpServerVenue
import playwrigkt.skript.venue.VertxVenue
import playwrigkt.skript.vertx.createApplication
import kotlin.math.floor

class VertxChatroomTransactionSpec: ChatroomTransactionsSpec() {

    companion object {
        val hikariConfig = JsonObject()
                .put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider")
                .put("jdbcUrl", "jdbc:postgresql://localhost:5432/chitchat")
                .put("username", "chatty_tammy")
                .put("password", "gossipy")
                .put("driver_class", "org.postgresql.Driver")
                .put("maximumPoolSize", 1)
                .put("poolName", "test_pool")

        val vertx by lazy { Vertx.vertx() }

        val port = floor((Math.random() * 8000)).toInt() + 2000

        val application by lazy { createApplication(vertx, hikariConfig, port) }
    }

    override fun application(): ExampleApplication = application

    override fun afterSpec(description: Description, spec: Spec) {
        super.afterSpec(description, spec)
        val future = Future.future<Void>()
        vertx.close(future.completer())
        awaitSucceededFuture(VertxResult(future))
    }
}