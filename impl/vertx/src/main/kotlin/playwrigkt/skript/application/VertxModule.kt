package playwrigkt.skript.application

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.json.JsonObject
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.*
import playwrigkt.skript.stagemanager.*

class VertxModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> =
            listOf(
                    VertxLoader,
                    VertxPublishStageManagerLoader,
                    VertxHttpStageManagerLoader,
                    VertxSerializeSageManagerrLoader,
                    VertxSQLStagemanagerLoader)
}


object VertxLoader: ApplicationResourceLoader<Vertx> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "vertx"

    override val loadResource: Skript<ApplicationResourceLoader.Input, Vertx, SkriptApplicationLoader> =
            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                    .map { it.applicationResourceLoaderConfig.config.raw.toByteArray() }
                    .deserialize(VertxOptions::class.java)
                    .map { Vertx.vertx(it) }
}

object VertxPublishStageManagerLoader: ApplicationResourceLoader<VertxPublishStageManager> {
    override val dependencies: List<String> = listOf("vertx")
    override val name: String = "vertx-publish"

    override val loadResource: Skript<ApplicationResourceLoader.Input, VertxPublishStageManager, SkriptApplicationLoader> =
            loadExistingApplicationResourceSkript<Vertx>("vertx")
                    .map { VertxPublishStageManager(it.eventBus()) }
}

object VertxHttpStageManagerLoader: ApplicationResourceLoader<VertxHttpClientStageManager> {
    override val dependencies: List<String> = listOf("vertx")
    override val name: String = "vertx-http-client"

    override val loadResource: Skript<ApplicationResourceLoader.Input, VertxHttpClientStageManager, SkriptApplicationLoader> =
            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                    .both(
                            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                                    .map { it.applicationResourceLoaderConfig.config.raw.toByteArray() }
                                    .deserialize(HttpClientOptions::class.java),
                            loadExistingApplicationResourceSkript<Vertx>("vertx"))
                    .join { clientOptions, vertx -> VertxHttpClientStageManager(clientOptions, vertx) }

}

object VertxSerializeSageManagerrLoader: ApplicationResourceLoader<VertxSerializeStageManager> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "vertx-serialize"

    override val loadResource: Skript<ApplicationResourceLoader.Input, VertxSerializeStageManager, SkriptApplicationLoader> = Skript.map { VertxSerializeStageManager() }
}

object VertxSQLStagemanagerLoader: ApplicationResourceLoader<VertxSQLStageManager> {

    override val dependencies: List<String> = listOf("vertx")
    override val name: String = "vertx-sql"


    override val loadResource: Skript<ApplicationResourceLoader.Input, VertxSQLStageManager, SkriptApplicationLoader> =
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
                            loadExistingApplicationResourceSkript<Vertx>("vertx"))
                    .join { config, poolName, vertx ->
                        VertxSQLStageManager(vertx, config, poolName)
                    }

}
