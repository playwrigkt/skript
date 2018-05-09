package playwrigkt.skript.vertx

import io.vertx.core.http.HttpServerOptions
import playwrigkt.skript.ExampleApplication
import playwrigkt.skript.application.*
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.*
import playwrigkt.skript.venue.VertxHttpServerVenue
import playwrigkt.skript.venue.VertxVenue

fun createApplication(port: Int): AsyncResult<ExampleApplication> {
    val stageManagers = loadApplication
            .run("vertx-application.json", SkriptApplicationLoader.loader(ApplicationRegistry()))

    val applicationStageManagerResult = stageManagers
            .map { it.get("example-application") }
            .map { it as ApplicationStageManager }

    val vertxResult = stageManagers
            .map { it.get("vertx") }
            .map { it as VertxStageManager }
            .map { it.hireTroupe() }

    return vertxResult.flatMap { vertx ->
        applicationStageManagerResult.map { applicationStageManager ->
            ExampleApplication(
                    applicationStageManager,
                    VertxHttpServerVenue(vertx, HttpServerOptions().setPort(port)),
                    VertxVenue(vertx))
        }
    }
}
