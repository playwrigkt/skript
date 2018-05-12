package playwrigkt.skript.application

import playwrigkt.skript.Skript
import playwrigkt.skript.stagemanager.JacksonSerializeStageManager
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.SerializeTroupe

class CoroutineJacksonSerializeModule: SkriptModule {
    override fun loaders(): List<StageManagerLoader<*>> = listOf(CoroutineJacksonSerializeStageManagerLoader)
}

object CoroutineJacksonSerializeStageManagerLoader: StageManagerLoader<SerializeTroupe> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "coroutine-jackson-serialize"

    override val loadManager: Skript<StageManagerLoader.Input, out StageManager<SerializeTroupe>, SkriptApplicationLoader> =
            Skript.map { JacksonSerializeStageManager() }

}