package playwrigkt.skript.application

import org.funktionale.option.getOrElse
import org.funktionale.option.toOption
import org.funktionale.tries.Try
import org.slf4j.LoggerFactory
import playwrigkt.skript.All3
import playwrigkt.skript.Skript
import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.ex.*
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.LightweightSynchronized
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.venue.Venue
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock


class ProduktionManagerModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> =
        listOf(ProduktionManagerLoader)
}

data class ProduktionManagerConfig(val produktions: List<ProduktionConfig>)

data class ProduktionConfig(val className: String, val mappings: Map<String, HttpServer.Endpoint>)

object ProduktionManagerLoader: ApplicationResourceLoader<ProduktionsManager<*, *, *, *>> {
    val log = LoggerFactory.getLogger(this::class.java)

    override val dependencies: List<String> = listOf("stageManager", "venue")
    override val loadResource: Skript<ApplicationResourceLoader.Input, ProduktionsManager<*, *, *, *>, SkriptApplicationLoader>
        = All3(
            loadExistingApplicationResourceSkript<Venue<Any, Any, Any>>("venue"),
            loadExistingApplicationResourceSkript<StageManager<Any>>("stageManager"),
            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                    .mapTry { it.applicationResourceLoaderConfig.config.parseProductionManagerConfig() }
                    .andThen(parseProduktionManagerConfig))
            .join { venue, stageManager, rulesAndSkripts ->
                log.info("creating produktionsManager $venue, $rulesAndSkripts")
                ProduktionsManager(venue, stageManager, rulesAndSkripts)
            }
}




val getSkriptsFromClass: Skript<ProduktionConfig, Map<Any, Skript<Any, Any, Any>>, Any> = Skript.identity<ProduktionConfig, Any>()
        .split(Skript.identity<ProduktionConfig, Any>()
                .map { it.className }
                .map { Class.forName(it) }
                .both<Any, List<Method>>(
                        Skript.map { it.getField("INSTANCE").get(null) },
                        Skript.map { it.methods.filter { it.returnType.equals(Skript::class.java) } }))
        .join { produktionConfig, (instance, methods) ->
            methods
                    .mapNotNull { method ->
                        produktionConfig.mappings.get(method.name.removePrefix("get").decapitalize())
                                ?.let { rule  -> rule as Any to method.invoke(instance) as Skript<Any, Any, Any> }
                    }
                    .toMap()
        }

val parseProduktionManagerConfig: Skript<ProduktionManagerConfig, Map<Any, Skript<Any, Any, Any>>, Any> =
        Skript.identity<ProduktionManagerConfig, Any>()
                .map { it.produktions }
                .mapList(getSkriptsFromClass)
                .map { it.flatMap { it.toList() }.toMap() }


fun ConfigValue.parseProductionManagerConfig(): Try<ProduktionManagerConfig> =
        this.objekt()
                .flatMap { it.values.get("produktions")
                        .toOption()
                        .map { Try.Success(it) }
                        .getOrElse { Try.Failure<ConfigValue>(ConfigValue.Error.ValueNotFound(".", "produktions")) }
                }
                .flatMap { it.array() }
                .flatMap { it.list.map { it.objekt() }.lift() }
                .flatMap { it.map { it.parseProduktionConfig() }.lift() }
                .map { ProduktionManagerConfig(it) }



fun ConfigValue.Collection.Object.parseProduktionConfig(): Try<ProduktionConfig> =
    this.applyPath("className", ".")
            .flatMap { it.text() }
            .map { it.value }
            .flatMap { className ->
                this.applyPath("mappings", ".")
                        .flatMap { it.objekt() }
                        .flatMap { it.values.mapValues { it.value.toHttpEndpoint() }.lift() }
                        .map { ProduktionConfig(className, it) }

            }

fun ConfigValue.toHttpEndpoint(): Try<HttpServer.Endpoint> =
    this.applyPath("path", ".")
            .flatMap { it.text() }
            .map { it.value }
            .flatMap { path ->
                this.applyPath("headers", ".")
                        .flatMap { it.objekt() }
                        .flatMap { it.values
                                .mapValues {
                                    it.value
                                            .array()
                                            .flatMap { it.list.map { it.text().map { it.value } }.lift() }
                                }
                                .lift()
                        }
                        .flatMap {  headers ->
                            this.applyPath("method", ".")
                                    .flatMap { it.toHttpMethod() }
                                    .map { HttpServer.Endpoint(path, headers, it) }

                }
    }

fun ConfigValue.toHttpMethod(): Try<Http.Method> =
    this.text().map { when(it.value.toLowerCase()) {
        "get" -> Http.Method.Get
        "post" -> Http.Method.Post
        "put" -> Http.Method.Put
        "delete" -> Http.Method.Delete
        "options" -> Http.Method.Options
        "head" -> Http.Method.Head
        "trace" -> Http.Method.Trace
        "connect" -> Http.Method.Connect
        "patch" -> Http.Method.Patch
        else -> Http.Method.Other(it.value)
    } }

data class ProduktionsManager<Rule, Beginning, End, Troupe>(val httpServerVenue: Venue<Rule, Beginning, End>,
                                                            val stageManager: StageManager<Troupe>,
                                                            val rulesAndSkripts: Map<Rule, Skript<Beginning, End, Troupe>>): ApplicationResource {
    val produktionManagers: AsyncResult<List<ProduktionManager<Rule, Beginning, End, Troupe>>> = rulesAndSkripts
            .map {
                ProduktionManager.of(httpServerVenue, stageManager, it.key, it.value)
            }
            .lift()

    override fun tearDown(): AsyncResult<Unit> =
            produktionManagers
                    .flatMap { it
                            .map { it.stop() }
                            .lift()
                    }
                    .map { Unit }
}

data class ProduktionManager<Rule, Beginning, End, Troupe>(val venue: Venue<Rule, Beginning, End>,
                                                           val stageManager: StageManager<Troupe>,
                                                           val rule: Rule,
                                                           val skript: Skript<Beginning, End, Troupe>): LightweightSynchronized {
    val log = LoggerFactory.getLogger(this::class.java)

    override val lock: ReentrantLock = ReentrantLock()
    @Volatile private var restart = AtomicBoolean(true)
    private val produktion = AtomicReference<Produktion>()

    companion object {
        fun <Rule, Beginning, End, Troupe> of(
                venue: Venue<Rule, Beginning, End>,
                stageManager: StageManager<Troupe>,
                rule: Rule,
                skript: Skript<Beginning, End, Troupe>): AsyncResult<ProduktionManager<Rule, Beginning, End, Troupe>> {
            val manager = ProduktionManager(venue, stageManager, rule, skript)
            return manager.startProduktion()
                    .map { manager }
        }

    }

    fun stop(): AsyncResult<Unit> =
        lock {
            log.info("stopping produktion.. $rule")
            this.restart.set(false)
            AsyncResult.succeeded(Unit)
        }

    private fun applyRestart(result: AsyncResult<out Produktion>) = result
            .flatMap {
                lock {
                    log.info("created produktion.. $rule")
                    this.produktion.set(it)
                }
                it.result()
            }
            .addHandler {
                lock {
                    if(restart.get()) {
                        log.info("restarting.. $rule")
                        this.startProduktion()
                    } else {
                        log.info("stopping.. $rule")
                        it.result?.let { AsyncResult.succeeded(it) }
                                ?: it.error?.let { AsyncResult.failed<Unit>(it) }
                                ?: AsyncResult.failed(IllegalStateException("result is neither success nor failure"))
                    }
                }
            }

    private fun startProduktion(): AsyncResult<out Produktion> =
            venue.produktion(skript, stageManager, rule)
                    .let {
                        applyRestart(it)
                        it
                    }
}
