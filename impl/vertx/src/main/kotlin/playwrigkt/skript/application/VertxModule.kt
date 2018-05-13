package playwrigkt.skript.application

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.all
import playwrigkt.skript.ex.deserialize
import playwrigkt.skript.ex.join
import playwrigkt.skript.stagemanager.VertxHttpClientStageManager
import playwrigkt.skript.stagemanager.VertxPublishStageManager
import playwrigkt.skript.stagemanager.VertxSqlStageManager
import playwrigkt.skript.stagemanager.VertxSerializeStageManager
import playwrigkt.skript.venue.VertxHttpServerVenue
import playwrigkt.skript.venue.VertxVenue

class VertxModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> =
            listOf(
                    VertxLoader,
                    VertxPublishStageManagerLoader,
                    VertxHttpClientStageManagerLoader,
                    VertxSerializeStageManagerLoader,
                    VertxSqlStageManagerLoader,
                    VertxHttpServerVenueLoader,
                    VertxVenueLoader)
}


object VertxLoader: ApplicationResourceLoader<Vertx> {
    override val dependencies: List<String> = emptyList()
    override val loadResource: Skript<ApplicationResourceLoader.Input, Vertx, SkriptApplicationLoader> =
            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                    .map { it.applicationResourceLoaderConfig.config.raw.toByteArray() }
                    .deserialize(VertxOptions::class.java)
                    .map { Vertx.vertx(it) }
}

object VertxPublishStageManagerLoader: ApplicationResourceLoader<VertxPublishStageManager> {
    override val dependencies: List<String> = listOf(VertxLoader.name())
    override val loadResource: Skript<ApplicationResourceLoader.Input, VertxPublishStageManager, SkriptApplicationLoader> =
            loadExistingApplicationResourceSkript<Vertx>(VertxLoader.name())
                    .map { VertxPublishStageManager(it.eventBus()) }
}

object VertxHttpClientStageManagerLoader: ApplicationResourceLoader<VertxHttpClientStageManager> {
    override val dependencies: List<String> = listOf(VertxLoader.name())
    override val loadResource: Skript<ApplicationResourceLoader.Input, VertxHttpClientStageManager, SkriptApplicationLoader> =
            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                    .both(
                            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                                    .map { it.applicationResourceLoaderConfig.config.raw.toByteArray() }
                                    .deserialize(HttpClientOptions::class.java),
                            loadExistingApplicationResourceSkript<Vertx>(VertxLoader.name()))
                    .join { clientOptions, vertx -> VertxHttpClientStageManager(clientOptions, vertx) }

}

object VertxSerializeStageManagerLoader: ApplicationResourceLoader<VertxSerializeStageManager> {
    override val dependencies: List<String> = emptyList()
    override val loadResource: Skript<ApplicationResourceLoader.Input, VertxSerializeStageManager, SkriptApplicationLoader> = Skript.map { VertxSerializeStageManager() }
}

object VertxSqlStageManagerLoader: ApplicationResourceLoader<VertxSqlStageManager> {

    override val dependencies: List<String> = listOf(VertxLoader.name())

    override val loadResource: Skript<ApplicationResourceLoader.Input, VertxSqlStageManager, SkriptApplicationLoader> =
            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                    .all(
                            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                                    .mapTry { it.applicationResourceLoaderConfig.config.applyPath("sql", ".") }
                                    .map { it.raw }
                                    .map { JsonObject(it) },
                            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                                    .mapTry { it.applicationResourceLoaderConfig.config.applyPath("sql.poolName", ".") }
                                    .mapTry {  it.text() }
                                    .map { it.value },
                            loadExistingApplicationResourceSkript<Vertx>(VertxLoader.name()))
                    .join { config, poolName, vertx ->
                        VertxSqlStageManager(vertx, config, poolName)
                    }

}

object VertxHttpServerVenueLoader: ApplicationResourceLoader<VertxHttpServerVenue> {
    override val dependencies: List<String> = listOf(VertxLoader.name())
    override val loadResource: Skript<ApplicationResourceLoader.Input, VertxHttpServerVenue, SkriptApplicationLoader> =
            Skript.both(
                    loadExistingApplicationResourceSkript<Vertx>(VertxLoader.name()),
                    Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                            .mapTry { it.applicationResourceLoaderConfig.config.applyPath("httpServer", ".") }
                            .map { it.raw.toByteArray() }
                            .deserialize(HttpServerOptions::class.java))
                    .join { vertx, httpServerOptions ->
                        VertxHttpServerVenue(vertx, httpServerOptions)
                    }
}

object VertxVenueLoader: ApplicationResourceLoader<VertxVenue> {
    override val dependencies: List<String> = listOf(VertxLoader.name())
    override val loadResource: Skript<ApplicationResourceLoader.Input, VertxVenue, SkriptApplicationLoader> =
            loadExistingApplicationResourceSkript<Vertx>(VertxLoader.name())
                    .map { VertxVenue(it) }
}