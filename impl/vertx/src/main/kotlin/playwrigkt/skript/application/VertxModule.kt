package playwrigkt.skript.application

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.json.JsonObject
import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.ex.all
import playwrigkt.skript.ex.join
import playwrigkt.skript.ex.joinTry
import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.result.AsyncResult
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

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<Vertx>> {
        //TODO read vertxOptions from config
        val vertxOptions = VertxOptions()

        return Try { VertxStageManager(Vertx.vertx(vertxOptions)) }.toAsyncResult()
    }

}
object VertxPublishStageManagerLoader: StageManagerLoader<QueuePublishTroupe> {
    override val dependencies: List<String> = listOf("vertx")
    override val name: String = "vertx-publish"

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<VertxPublishStageManager> =
        loadExisting<Vertx>("vertx", existingManagers, config)
                .toAsyncResult()
                .map { it.hireTroupe() }
                .map { it.eventBus() }
                .map { VertxPublishStageManager(it) }
}

object VertxHttpStageManagerLoader: StageManagerLoader<HttpClientTroupe> {
    override val dependencies: List<String> = listOf("vertx")
    override val name: String = "vertx-http-client"
    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<HttpClientTroupe>> {
        //TODO read client options from config
        val clientOptions = HttpClientOptions()

        return loadExisting<Vertx>("vertx", existingManagers, config)
                .toAsyncResult()
                .map { it.hireTroupe() }
                .map { VertxHttpRequestStageManager(clientOptions, it)}
    }

}

object VertxSerializeSageManagerrLoader: StageManagerLoader<SerializeTroupe> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "vertx-serialize"
    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<SerializeTroupe>> {
        return Try { VertxSerializeStageManager() }.toAsyncResult()
    }
}

object VertxSQLStagemanagerLoader: StageManagerLoader<SQLTroupe> {
    override val dependencies: List<String> = listOf("vertx")
    override val name: String = "vertx-sql"

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<SQLTroupe>> =
            skript.run(Pair(existingManagers, config), Unit)

    val skript = Skript.identity<Pair<Map<String, StageManager<*>>, StageManagerLoaderConfig>, Unit>()
            .all(
                    Skript.identity<Pair<Map<String, StageManager<*>>, StageManagerLoaderConfig>, Unit>()
                            .joinTry { _, config -> config.config.applyPath("sql", ".") }
                            .mapTry{ it.objekt() }
                            .map { it.toJsonObject() },
                    Skript.identity<Pair<Map<String, StageManager<*>>, StageManagerLoaderConfig>, Unit>()
                            .joinTry { _, config -> config.config.applyPath("sql.poolName", ".") }
                            .mapTry {  it.text() }
                            .map { it.value },
                    Skript.identity<Pair<Map<String, StageManager<*>>, StageManagerLoaderConfig>, Unit>()
                            .joinTry { existingManagers, config -> loadExisting<Vertx>("vertx", existingManagers, config) })
            .join { config, poolName, vertx ->
                VertxSQLStageManager(vertx.hireTroupe(), config, poolName)
            }

}

fun ConfigValue.Collection.Object.toJsonObject(): JsonObject =
            this.values
                    .mapValues { entry -> entry.value.toJson() }
                    .let(JsonObject::mapFrom)

fun ConfigValue.toJson(): Any? =
    when {
        this is ConfigValue.Text -> this.value
        this is ConfigValue.Number -> {
            Try<Any> { this.value.intValueExact() }
                    .orElse { Try { this.value.longValueExact() } }
                    .getOrElse { this }
        }
        this is ConfigValue.Decimal -> {
            Try<Any> { this.value.toDouble() }
        }
        this is ConfigValue.Bool -> this.value
        this is ConfigValue.Collection.Array -> this.list.map { it.toJson() }
        this is ConfigValue.Collection.Object ->
            this.values.mapValues { it.value.toJson() }.let(JsonObject::mapFrom)
        else -> null
    }
