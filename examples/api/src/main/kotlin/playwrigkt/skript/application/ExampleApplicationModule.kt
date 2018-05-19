package playwrigkt.skript.application

import playwrigkt.skript.ExampleApplication
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.all
import playwrigkt.skript.ex.join
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.*
import playwrigkt.skript.venue.HttpServerVenue
import playwrigkt.skript.venue.QueueVenue

class ExampleApplicationModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> =
            listOf(ExampleApplicationStageManagerLoader)
}

object ExampleApplicationStageManagerLoader: ApplicationResourceLoader<ApplicationStageManager> {
    override val dependencies: List<String> = listOf("sql", "publish", "serialize", "http-client")

    override val loadResource =
            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                    .all(
                            loadExistingApplicationResourceSkript<StageManager<SqlTroupe>>("sql"),
                            loadExistingApplicationResourceSkript<StageManager<SerializeTroupe>>("serialize"),
                            loadExistingApplicationResourceSkript<StageManager<HttpClientTroupe>>("http-client"),
                            loadExistingApplicationResourceSkript<StageManager<QueuePublishTroupe>>("publish"))
                    .join { sql, serialize, httpClient, publish ->
                        ApplicationStageManager(publish, sql, serialize, httpClient)
                    }
}