package playwrigkt.skript.application

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.json.JsonObject
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.*
import playwrigkt.skript.stagemanager.*
import playwrigkt.skript.troupe.*

class VertxModule: SkriptModule {
    override fun loaders(): List<StageManagerLoader<*>> =
            listOf(
                    VertxStageManagerLoader,
                    VertxPublishStageManagerLoader,
                    VertxHttpStageManagerLoader,
                    VertxSerializeSageManagerrLoader,
                    VertxSQLStagemanagerLoader)
}


object VertxStageManagerLoader: StageManagerLoader<Vertx> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "vertx"

    override val loadManager: Skript<StageManagerLoader.Input, out StageManager<Vertx>, SkriptApplicationLoader> =
            Skript.identity<StageManagerLoader.Input, SkriptApplicationLoader>()
                    .map { it.stageManagerLoaderConfig.config.raw.toByteArray() }
                    .deserialize(VertxOptions::class.java)
                    .map { Vertx.vertx(it) }
                    .map { VertxStageManager(it) }
}

object VertxPublishStageManagerLoader: StageManagerLoader<QueuePublishTroupe> {
    override val dependencies: List<String> = listOf("vertx")
    override val name: String = "vertx-publish"

    override val loadManager: Skript<StageManagerLoader.Input, out StageManager<QueuePublishTroupe>, SkriptApplicationLoader> =
            loadExistingStageManagerSkript<Vertx>("vertx")
                    .map { it.hireTroupe().eventBus() }
                    .map { VertxPublishStageManager(it) }
}

object VertxHttpStageManagerLoader: StageManagerLoader<HttpClientTroupe> {
    override val dependencies: List<String> = listOf("vertx")
    override val name: String = "vertx-http-client"

    override val loadManager: Skript<StageManagerLoader.Input, out StageManager<HttpClientTroupe>, SkriptApplicationLoader> =
            Skript.identity<StageManagerLoader.Input, SkriptApplicationLoader>()
                    .both(
                            Skript.identity<StageManagerLoader.Input, SkriptApplicationLoader>()
                                    .map { it.stageManagerLoaderConfig.config.raw.toByteArray() }
                                    .deserialize(HttpClientOptions::class.java),
                            loadExistingStageManagerSkript<Vertx>("vertx").map { it.hireTroupe() })
                    .join { clientOptions, vertx -> VertxHttpClientStageManager(clientOptions, vertx) }

}

object VertxSerializeSageManagerrLoader: StageManagerLoader<SerializeTroupe> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "vertx-serialize"

    override val loadManager: Skript<StageManagerLoader.Input, VertxSerializeStageManager, SkriptApplicationLoader> = Skript.map { VertxSerializeStageManager() }
}

object VertxSQLStagemanagerLoader: StageManagerLoader<SQLTroupe> {

    override val dependencies: List<String> = listOf("vertx")
    override val name: String = "vertx-sql"


    override val loadManager: Skript<StageManagerLoader.Input, VertxSQLStageManager, SkriptApplicationLoader> =
            Skript.identity<StageManagerLoader.Input, SkriptApplicationLoader>()
                    .all(
                            Skript.identity<StageManagerLoader.Input, SkriptApplicationLoader>()
                                    .mapTry { it.stageManagerLoaderConfig.config.applyPath("sql", ".") }
                                    .map { it.raw }
                                    .map { JsonObject(it) },
                            Skript.identity<StageManagerLoader.Input, SkriptApplicationLoader>()
                                    .mapTry { it.stageManagerLoaderConfig.config.applyPath("sql.poolName", ".") }
                                    .mapTry {  it.text() }
                                    .map { it.value },
                            loadExistingStageManagerSkript<Vertx>("vertx"))
                    .join { config, poolName, vertx ->
                        VertxSQLStageManager(vertx.hireTroupe(), config, poolName)
                    }

}
