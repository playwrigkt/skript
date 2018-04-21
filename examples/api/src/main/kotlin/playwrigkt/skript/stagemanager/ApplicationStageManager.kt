package playwrigkt.skript.stagemanager

import playwrigkt.skript.Skript
import playwrigkt.skript.ex.lift
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.*
import playwrigkt.skript.user.http.createUserHttpEndpointSkript
import playwrigkt.skript.user.http.getUserHttpEndpointSkript
import playwrigkt.skript.user.http.loginUserHttpEndpointSkript
import playwrigkt.skript.venue.HttpServerVenue
import playwrigkt.skript.venue.QueueVenue

data class ApplicationStageManager(
        val publishProvider: StageManager<QueuePublishTroupe>,
        val sqlProvider: StageManager<SQLTroupe>,
        val serializeProvider: StageManager<SerializeTroupe>,
        val httpManager: StageManager<HttpClientTroupe>,
        val httpServerVenue: HttpServerVenue,
        val queueVenue: QueueVenue
): StageManager<ApplicationTroupe> {

    companion object {
        val userCreatedAddress = "user.updated"
        val userLoginAddress = "user.login"
    }
    override fun hireTroupe(): ApplicationTroupe =
            ApplicationTroupe(publishProvider.hireTroupe(), sqlProvider.hireTroupe(), serializeProvider.hireTroupe(), httpManager.hireTroupe())

    fun <I, O> runWithTroupe(skript: Skript<I, O, ApplicationTroupe>, i: I): AsyncResult<O> {
        return skript.run(i, hireTroupe())
    }

    val userProduktions by lazy {
        listOf(
                httpServerVenue.produktion(
                        createUserHttpEndpointSkript,
                        this,
                        HttpServer.Endpoint(
                                "/users",
                                emptyMap(),
                                Http.Method.Post)),
                httpServerVenue.produktion(
                        loginUserHttpEndpointSkript,
                        this,
                        HttpServer.Endpoint(
                                "/login",
                                emptyMap(),
                                Http.Method.Post)),
                httpServerVenue.produktion(
                        getUserHttpEndpointSkript,
                        this,
                        HttpServer.Endpoint(
                                "/users/{userId}",
                                mapOf("Authorization" to emptyList()),
                                Http.Method.Get)))
                .lift()
    }

    fun userLoginQueueProduktion(skript: Skript<QueueMessage, Unit, ApplicationTroupe>) =
            queueVenue.produktion(
                    skript,
                    this,
                    userLoginAddress)

    fun userCreateQueueProduktion(skript: Skript<QueueMessage, Unit, ApplicationTroupe>) =
            queueVenue.produktion(
                    skript,
                    this,
                    userCreatedAddress)

    override fun tearDown(): AsyncResult<Unit> =
        listOf(publishProvider.tearDown(), sqlProvider.tearDown(), serializeProvider.tearDown(), httpManager.tearDown(), queueVenue.teardown(), httpServerVenue.teardown())
                .lift()
                .map { Unit }
}

