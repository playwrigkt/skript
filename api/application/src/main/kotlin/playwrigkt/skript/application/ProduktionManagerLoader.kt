package playwrigkt.skript.application

import arrow.core.getOrElse
import arrow.core.toOption
import arrow.core.Try
import org.slf4j.LoggerFactory
import playwrigkt.skript.All3
import playwrigkt.skript.Skript
import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.ex.*
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.produktion.ProduktionConfig
import playwrigkt.skript.produktion.ProduktionManagerConfig
import playwrigkt.skript.produktion.ProduktionsManager
import playwrigkt.skript.venue.Venue
import java.lang.reflect.Method

abstract class ProduktionManagerLoader<Rule: Any, Beginning, End, Troupe>: ApplicationResourceLoader<ProduktionsManager<Rule, Beginning, End, Troupe>> {
    val log = LoggerFactory.getLogger(this::class.java)

    abstract val parseRuleConfig: Skript<ConfigValue, Rule, SkriptApplicationLoader>

    override val dependencies: List<String> = listOf("stageManager", "venue")

    override val loadResource: Skript<ApplicationResourceLoader.Input, ProduktionsManager<Rule, Beginning, End, Troupe>, SkriptApplicationLoader>by lazy {
                All3(
                        loadExistingApplicationResourceSkript<Venue<Rule, Beginning, End>>("venue"),
                        loadExistingApplicationResourceSkript<StageManager<Troupe>>("stageManager"),
                        Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                                .andThen(parseProduktionsManagerConfig)
                                .andThen(buildRuleMapping))
                        .join { venue, stageManager, rulesAndSkripts ->
                            log.info("creating produktionsManager $venue, $rulesAndSkripts")
                            ProduktionsManager(venue, stageManager, rulesAndSkripts)
                        }
            }

    private val parseProduktionsManagerConfig by lazy {
        Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                .mapTry { it.applicationResourceLoaderConfig.config.objekt() }
                .mapTry {
                    it.values.get("produktions")
                            .toOption()
                            .map { Try.Success(it) }
                            .getOrElse { Try.Failure<ConfigValue>(ConfigValue.Error.ValueNotFound(".", "produktions")) }
                }
                .mapTry { it.array() }
                .mapTry { it.list.map { it.objekt() }.liftTry() }
                .iterate(parseProduktionConfig)
                .map { ProduktionManagerConfig(it) }
    }

    private val parseProduktionConfig by lazy {
        Skript.identity<ConfigValue, SkriptApplicationLoader>()
                .both(
                        Skript.identity<ConfigValue, SkriptApplicationLoader>()
                                .mapTry { it.objekt() }
                                .mapTry { it.applyPath("className", ".") }
                                .mapTry { it.text() }
                                .map { it.value },
                        Skript.identity<ConfigValue, SkriptApplicationLoader>()
                                .mapTry { it.applyPath("mappings", ".") }
                                .mapTry { it.objekt() }
                                .map { it.values }
                                .iterateValues(parseRuleConfig))
                .join { className, mappings -> ProduktionConfig(className, mappings) }
    }

    private val buildRuleMapping by lazy {
        Skript.identity<ProduktionManagerConfig<Rule>, Any>()
                .map { it.produktions }
                .iterate(getSkriptsFromClass)
                .map { it.flatMap { it.toList() }.toMap() }
    }

    val getSkriptsFromClass: Skript<ProduktionConfig<Rule>, Map<Rule, Skript<Beginning, End, Troupe>>, Any> by lazy {
        Skript.identity<ProduktionConfig<Rule>, Any>()
                .split(Skript.identity<ProduktionConfig<Rule>, Any>()
                        .map { it.className }
                        .map { Class.forName(it) }
                        .both<Any, List<Method>>(
                                Skript.map { it.getField("INSTANCE").get(null) },
                                Skript.map { it.methods.filter { it.returnType.equals(Skript::class.java) } }))
                .join { produktionConfig, (instance, methods) ->
                    methods
                            .mapNotNull { method ->
                                produktionConfig.mappings.get(method.name.removePrefix("get").decapitalize())
                                        ?.let { rule -> rule to method.invoke(instance) as Skript<Beginning, End, Troupe> }
                            }
                            .toMap()
                }
    }
}