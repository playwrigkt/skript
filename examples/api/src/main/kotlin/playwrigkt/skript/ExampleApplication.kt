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

fun createApplication(configFile: String): AsyncResult<ExampleApplication> {
    val loader = SkriptApplicationLoader(SyncFileTroupe, SyncJacksonSerializeStageManager().hireTroupe(), ApplicationRegistry())

    return loadApplication
            .run(configFile, loader)
            .map { it.applicationResources.get(ExampleApplicationLoader.name()) }
            .map { it as ExampleApplication }
}

data class ExampleApplication(val stageManager: ApplicationStageManager,
                              val httpServerVenue: HttpServerVenue,
                              val queueVenue: QueueVenue,
                              val httpProduktionManager: ProduktionsManager<HttpServer.Endpoint, HttpServer.Request<ByteArray>, HttpServer.Response, ApplicationTroupe>) {
    companion object {
        val userCreatedAddress = "user.updated"
        val userLoginAddress = "user.login"
    }

    val startResult = httpProduktionManager.produktionManagers.map { Unit }

    fun queueConsumerProduktion(queue: String, skript: Skript<QueueMessage, Unit, ApplicationTroupe>): AsyncResult<out Produktion> =
        queueVenue.produktion(skript, stageManager, queue)

    fun teardown(): AsyncResult<List<Unit>> =
        listOf(httpProduktionManager.tearDown(), stageManager.tearDown(), httpServerVenue.teardown(), queueVenue.teardown())
                .lift()
}


