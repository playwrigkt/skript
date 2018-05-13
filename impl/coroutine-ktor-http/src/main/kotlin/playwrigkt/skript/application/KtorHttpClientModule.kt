package playwrigkt.skript.application

import playwrigkt.skript.Skript
import playwrigkt.skript.stagemanager.KtorHttpClientStageManager

class KtorHttpClientModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> = listOf(KtorHttpClientStageManagerLoader)
}

object KtorHttpClientStageManagerLoader: ApplicationResourceLoader<KtorHttpClientStageManager> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "ktor-http-client"

    override val loadResource: Skript<ApplicationResourceLoader.Input, KtorHttpClientStageManager, SkriptApplicationLoader> =
            Skript.map { KtorHttpClientStageManager() }
}
