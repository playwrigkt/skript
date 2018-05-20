package playwrigkt.skript.application

import playwrigkt.skript.Skript
import playwrigkt.skript.ex.all
import playwrigkt.skript.ex.join
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.*

class ExampleApplicationModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> =
            listOf(ExampleApplicationStageManagerLoader)
}

object ExampleApplicationStageManagerLoader: ApplicationResourceLoader<ApplicationStageManager> {
    override val dependencies: List<String> = listOf(
            "sqlStageManager",
            "publishStageManager",
            "serializeStageManager",
            "httpClientStageManager",
            "configurationStageManager")

    override val loadResource =
            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                    .all(
                            loadExistingApplicationResourceSkript<StageManager<SqlTroupe>>("sqlStageManager"),
                            loadExistingApplicationResourceSkript<StageManager<SerializeTroupe>>("serializeStageManager"),
                            loadExistingApplicationResourceSkript<StageManager<HttpClientTroupe>>("httpClientStageManager"),
                            loadExistingApplicationResourceSkript<StageManager<QueuePublishTroupe>>("publishStageManager"),
                            loadExistingApplicationResourceSkript<StageManager<ConfigTroupe>>("configurationStageManager"))
                    .join { sql, serialize, httpClient, publish, config ->
                        ApplicationStageManager(publish, sql, serialize, httpClient, config)
                    }
}