package playwrigkt.skript

import playwrigkt.skript.application.SkriptModule
import playwrigkt.skript.application.StageManagerLoader
import playwrigkt.skript.application.StageManagerLoaderConfig
import playwrigkt.skript.ex.all
import playwrigkt.skript.ex.join
import playwrigkt.skript.ex.joinTry
import playwrigkt.skript.ex.lift
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.*
import playwrigkt.skript.user.http.createUserHttpEndpointSkript
import playwrigkt.skript.user.http.getUserHttpEndpointSkript
import playwrigkt.skript.user.http.loginUserHttpEndpointSkript
import playwrigkt.skript.venue.HttpServerVenue
import playwrigkt.skript.venue.QueueVenue

data class ExampleApplication(val stageManager: ApplicationStageManager,
                              val httpServerVenue: HttpServerVenue,
                              val queueVenue: QueueVenue) {
    companion object {
        val userCreatedAddress = "user.updated"
        val userLoginAddress = "user.login"
    }

    fun loadHttpProduktions(): AsyncResult<List<Produktion>> =
        listOf(createUserHttpProduktion(),
                loginuserHttpProduktion(),
                getUserHttpProduktion())
                .lift()

    fun queueConsumerProduktion(queue: String, skript: Skript<QueueMessage, Unit, ApplicationTroupe>): AsyncResult<out Produktion> =
        queueVenue.produktion(skript, stageManager, queue)

    fun teardown(): AsyncResult<List<Unit>> =
        listOf(stageManager.tearDown(), httpServerVenue.teardown(), queueVenue.teardown())
                .lift()

    private fun createUserHttpProduktion() =
            httpServerVenue.produktion(
                    createUserHttpEndpointSkript,
                    stageManager,
                    HttpServer.Endpoint(
                            "/users",
                            emptyMap(),
                            Http.Method.Post))

    private fun loginuserHttpProduktion() =
            httpServerVenue.produktion(
                    loginUserHttpEndpointSkript,
                    stageManager,
                    HttpServer.Endpoint(
                            "/login",
                            emptyMap(),
                            Http.Method.Post))

    private fun getUserHttpProduktion() =
            httpServerVenue.produktion(
                    getUserHttpEndpointSkript,
                    stageManager,
                    HttpServer.Endpoint(
                            "/users/{userId}",
                            mapOf("Authorization" to emptyList()),
                            Http.Method.Get))
}

typealias LoaderInput = Pair<Map<String, StageManager<*>>, StageManagerLoaderConfig>

class ExampleApplicationModule: SkriptModule {
    override fun loaders(): List<StageManagerLoader<*>> =
            listOf(ExampleApplicationStageManagerLoader)
}

object ExampleApplicationStageManagerLoader: StageManagerLoader<ApplicationTroupe> {
    override val dependencies: List<String> = listOf("sql", "publish", "serialize", "http-client")
    override val name: String = "example-application"

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<ApplicationStageManager> =
            loadSkript.run(Pair(existingManagers, config), Unit)



    val loadSkript =
            Skript.identity<Pair<Map<String, StageManager<*>>, StageManagerLoaderConfig>, Unit>()
                    .all(
                            loadExistingStageManagerSkript<SQLTroupe>("sql"),
                            loadExistingStageManagerSkript<SerializeTroupe>("serialize"),
                            loadExistingStageManagerSkript<HttpClientTroupe>("http-client"),
                            loadExistingStageManagerSkript<QueuePublishTroupe>("publish"))
                    .join { sql, serialize, httpClient, publish ->
                        ApplicationStageManager(publish, sql, serialize, httpClient)
                    }

    private fun <Troupe> loadExistingStageManagerSkript(name: String): Skript<LoaderInput, StageManager<Troupe>, Unit> =
            Skript.identity<LoaderInput, Unit>()
                    .joinTry{ existingManagers, config ->
                        loadExisting<Troupe>(name, existingManagers, config) }
}