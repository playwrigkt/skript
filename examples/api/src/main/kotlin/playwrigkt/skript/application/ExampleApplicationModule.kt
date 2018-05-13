package playwrigkt.skript.application

import playwrigkt.skript.ExampleApplication
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.all
import playwrigkt.skript.ex.join
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.HttpClientTroupe
import playwrigkt.skript.troupe.QueuePublishTroupe
import playwrigkt.skript.troupe.SqlTroupe
import playwrigkt.skript.troupe.SerializeTroupe
import playwrigkt.skript.venue.HttpServerVenue
import playwrigkt.skript.venue.QueueVenue

class ExampleApplicationModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> =
            listOf(ExampleApplicationStageManagerLoader,
                    ExampleApplicationLoader)
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

object ExampleApplicationLoader: ApplicationResourceLoader<ExampleApplication> {
    override val dependencies: List<String> = listOf(ExampleApplicationStageManagerLoader.name(), "http-server-venue", "queue-venue")

    override val loadResource: Skript<ApplicationResourceLoader.Input, ExampleApplication, SkriptApplicationLoader> =
            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                    .all(
                            loadExistingApplicationResourceSkript<ApplicationStageManager>(ExampleApplicationStageManagerLoader.name()),
                            loadExistingApplicationResourceSkript<HttpServerVenue>("http-server-venue"),
                            loadExistingApplicationResourceSkript<QueueVenue>("queue-venue"))
                    .join { applicationStageManager, httpServerVenue, queueVenue->
                        ExampleApplication(applicationStageManager, httpServerVenue, queueVenue)
                    }
}