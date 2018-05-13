package playwrigkt.skript.application

import playwrigkt.skript.Skript
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.stagemanager.SyncJacksonSerializeStageManager
import playwrigkt.skript.troupe.SerializeTroupe

object SyncJacksonSerializeStageManagerLoader: ApplicationResourceLoader<SyncJacksonSerializeStageManager> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "serialize"

    override val loadResource: Skript<ApplicationResourceLoader.Input, SyncJacksonSerializeStageManager, SkriptApplicationLoader> =
            Skript.map { SyncJacksonSerializeStageManager() }
}