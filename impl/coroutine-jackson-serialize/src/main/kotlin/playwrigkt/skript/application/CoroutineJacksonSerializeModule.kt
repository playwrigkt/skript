package playwrigkt.skript.application

import playwrigkt.skript.Skript
import playwrigkt.skript.stagemanager.JacksonSerializeStageManager

class CoroutineJacksonSerializeModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> = listOf(CoroutineJacksonSerializeStageManagerLoader)
}

object CoroutineJacksonSerializeStageManagerLoader: ApplicationResourceLoader<JacksonSerializeStageManager> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "coroutine-jackson-serialize"

    override val loadResource: Skript<ApplicationResourceLoader.Input, JacksonSerializeStageManager, SkriptApplicationLoader> =
            Skript.map { JacksonSerializeStageManager() }

}