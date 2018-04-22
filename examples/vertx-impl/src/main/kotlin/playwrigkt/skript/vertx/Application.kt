package playwrigkt.skript.vertx

import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import playwrigkt.skript.ExampleApplication
import playwrigkt.skript.stagemanager.*
import playwrigkt.skript.venue.VertxHttpServerVenue
import playwrigkt.skript.venue.VertxVenue

fun createApplication(vertx: Vertx, sqlConfig: JsonObject, port: Int): ExampleApplication {
    val vertxVenue = VertxVenue(vertx)
    val httpServerVenue = VertxHttpServerVenue(vertx, HttpServerOptions().setPort(port))

    val sqlConnectionStageManager = VertxSQLStageManager(vertx, sqlConfig, "application_datasource")
    val publishStageManager = VertxPublishStageManager(vertx.eventBus())
    val serializeStageManager = VertxSerializeStageManager()
    val httpStageManager = VertxHttpRequestStageManager(HttpClientOptions().setDefaultPort(port), vertx)
    val stageManager = ApplicationStageManager(publishStageManager, sqlConnectionStageManager, serializeStageManager, httpStageManager)
    return ExampleApplication(stageManager, httpServerVenue, vertxVenue)
}