package playwrigkt.skript.application

import playwrigkt.skript.Skript
import playwrigkt.skript.stagemanager.SyncJacksonSerializeStageManager

object SyncJacksonSerializeStageManagerLoader: ApplicationResourceLoader<SyncJacksonSerializeStageManager> {
    override val dependencies: List<String> = emptyList()

    override val loadResource: Skript<ApplicationResourceLoader.Input, SyncJacksonSerializeStageManager, SkriptApplicationLoader> =
            Skript.map { SyncJacksonSerializeStageManager() }
}