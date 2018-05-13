package playwrigkt.skript.application

import playwrigkt.skript.Skript
import playwrigkt.skript.ex.join
import playwrigkt.skript.stagemanager.KtorHttpClientStageManager
import playwrigkt.skript.venue.KtorHttpServerVenue

class KtorHttpClientModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> = listOf(KtorHttpClientStageManagerLoader, KtorHttpServerVenueLoader)
}

object KtorHttpClientStageManagerLoader: ApplicationResourceLoader<KtorHttpClientStageManager> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "ktor-http-client"

    override val loadResource: Skript<ApplicationResourceLoader.Input, KtorHttpClientStageManager, SkriptApplicationLoader> =
            Skript.map { KtorHttpClientStageManager() }
}

object KtorHttpServerVenueLoader: ApplicationResourceLoader<KtorHttpServerVenue> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "ktor-http-server"

    override val loadResource: Skript<ApplicationResourceLoader.Input, KtorHttpServerVenue, SkriptApplicationLoader> =
            Skript.both(
                    Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                            .mapTry { it.applicationResourceLoaderConfig.config.applyPath("port", ".") }
                            .mapTry { it.number() },
                    Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                            .mapTry { it.applicationResourceLoaderConfig.config.applyPath("maxConnectionMillis", ".") }
                            .mapTry { it.number() }
                    )
                    .join { port, maxConnectionMillis -> KtorHttpServerVenue(port.value.intValueExact(), maxConnectionMillis.value.longValueExact())}
}