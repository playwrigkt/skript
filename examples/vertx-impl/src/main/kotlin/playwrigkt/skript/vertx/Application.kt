package playwrigkt.skript.vertx

import io.vertx.core.http.HttpServerOptions
import playwrigkt.skript.ExampleApplication
import playwrigkt.skript.ExampleApplicationStageManagerLoader
import playwrigkt.skript.application.*
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.*
import playwrigkt.skript.troupe.SyncFileTroupe
import playwrigkt.skript.venue.VertxHttpServerVenue
import playwrigkt.skript.venue.VertxVenue

fun createApplication(port: Int): AsyncResult<ExampleApplication> {
    val stageManagers = loadApplication
            .run("vertx-application.json", SkriptApplicationLoader(SyncFileTroupe, SyncJacksonSerializeStageManager().hireTroupe(), ApplicationRegistry()))
            .map { it.stageManagers }

    val applicationStageManagerResult = stageManagers
            .map { it.get(ExampleApplicationStageManagerLoader.name) }
            .map { it as ApplicationStageManager }

    val vertxResult = stageManagers
            .map { it.get(VertxStageManagerLoader.name) }
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
