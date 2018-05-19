package playwrigkt.skript

import playwrigkt.skript.application.*
import playwrigkt.skript.ex.lift
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.stagemanager.SyncJacksonSerializeStageManager
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.troupe.SyncFileTroupe
import playwrigkt.skript.venue.HttpServerVenue
import playwrigkt.skript.venue.QueueVenue

fun createApplication(configFile: String): AsyncResult<SkriptApplication> {
    val loader = SkriptApplicationLoader(SyncFileTroupe, SyncJacksonSerializeStageManager().hireTroupe(), ApplicationRegistry())

    return loadApplication
            .run(configFile, loader)
}

object ExampleApplication {
    val userCreatedAddress = "user.updated"
    val userLoginAddress = "user.login"
}


