package playwrigkt.skript.application

import playwrigkt.skript.Skript
import playwrigkt.skript.stagemanager.KtorHttpClientStageManager
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.KtorHttpClientTroupe

class KtorHttpClientModule: SkriptModule {
    override fun loaders(): List<StageManagerLoader<*>> = listOf(KtorHttpClientStageManagerLoader)
}

object KtorHttpClientStageManagerLoader: StageManagerLoader<KtorHttpClientTroupe> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "ktor-http-client"

    override val loadManager: Skript<StageManagerLoader.Input, out StageManager<KtorHttpClientTroupe>, SkriptApplicationLoader> =
            Skript.map { KtorHttpClientStageManager() }
}
