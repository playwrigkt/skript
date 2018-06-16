package playwrigkt.skript.application

import arrow.core.*
import org.slf4j.LoggerFactory
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.*
import playwrigkt.skript.file.FileReference
import playwrigkt.skript.performer.FilePerformer
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.FileTroupe
import playwrigkt.skript.troupe.SerializeTroupe

data class SkriptApplication(val applicationResources: Map<String, out ApplicationResource>,
                             val config: AppConfig,
                             val applicationRegistry: ApplicationRegistry) {
    val log = LoggerFactory.getLogger(this::class.java)

    fun tearDown() : AsyncResult<Unit> =
            tearDownInReverseDependencyOrder(config.applicationResourceLoaders)

    fun tearDownInReverseDependencyOrder(applicationResourceConfigs: List<ApplicationResourceLoaderConfig>): AsyncResult<Unit> {
        if(applicationResourceConfigs.isEmpty()) {
            return AsyncResult.succeeded(Unit)
        }

        val dependenciesMap = dependencyMap(applicationResourceConfigs)
        val hasNoDependencies = hasNoDependents(dependenciesMap)

        if(hasNoDependencies.isEmpty()) {
            return AsyncResult.failed(IllegalStateException("could not find dependencies that have  no running dependencies"))
        }

        return hasNoDependencies
                .mapNotNull { applicationResources.get(it) }
                .map {
                    log.info("Tearing down $it...")
                    it.tearDown()
                }
                .lift()
                .flatMap { tearDownInReverseDependencyOrder(applicationResourceConfigs.filterNot { hasNoDependencies.contains(it.name) }) }
    }

    fun dependencyMap(applicationResourceConfigs: List<ApplicationResourceLoaderConfig>): Map<String, List<String>> =
            applicationResourceConfigs
                    .map { config ->
                        config.name to applicationRegistry.getLoader(config.name)
                                .map { it.dependencies.map { config.applyOverride(it) } }
                                .getOrElse { emptyList() }
                    }
                    .toMap()

    fun hasNoDependents(dependenciesMap: Map<String, List<String>>): List<String> =
            dependenciesMap
                    .filterNot { dependenciesMap.values.flatten().toSet().contains(it.key) }
                    .map { it.key }
}

data class SkriptApplicationLoader(val fileTroupe: FileTroupe, val serializeTroupe: SerializeTroupe, val applicationRegistry: ApplicationRegistry): FileTroupe, SerializeTroupe {
        val log = LoggerFactory.getLogger(this::class.java)

        override fun getFilePerformer(): AsyncResult<out FilePerformer> = fileTroupe.getFilePerformer()

        override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = serializeTroupe.getSerializePerformer()


        fun buildApplication(appConfig: AppConfig): AsyncResult<SkriptApplication> =
                buildApplicationResources(appConfig.applicationResourceLoaders)
                        .map { SkriptApplication(it, appConfig, applicationRegistry) }
                        .onFailure { printFailureState(appConfig, it) }

        private fun buildApplicationResources(remainingApplicationResources: List<ApplicationResourceLoaderConfig>,
                                              completedApplicationResources: Map<String, ApplicationResource> = emptyMap()): AsyncResult<Map<String, ApplicationResource>> {

                if(remainingApplicationResources.isEmpty()) {
                        return AsyncResult.succeeded(completedApplicationResources)
                }

                val remainingAfter =
                        remainingApplicationResources.filterNot { config ->
                                applicationRegistry.dependenciesAreSatisfied(config, completedApplicationResources)
                        }

                if (remainingAfter.size.equals(remainingApplicationResources.size)) {
                        return AsyncResult.failed(ApplicationRegistry.RegistryException(ApplicationRegistry.RegistryError.UnsatisfiedDependencies(remainingAfter, completedApplicationResources)))
                }

                val result = remainingApplicationResources
                        .filter { config -> applicationRegistry.dependenciesAreSatisfied(config, completedApplicationResources) }
                        .map { config -> config to
                                applicationRegistry.getLoader(config.name)
                                        .recoverWith {
                                            config.implements
                                                    .toOption()
                                                    .filter { it.isNotBlank() }
                                                    .map(applicationRegistry::getLoader)
                                                    .getOrElse { Try.Failure(it) }
                                        }
                                        .map { ensureBothNamesRegistered(config, resourceLoader = it) }
                                        .toAsyncResult()
                                        .flatMap {
                                                log.info("loading application resource $config with $it")
                                                it.loadResource.run(ApplicationResourceLoader.Input(completedApplicationResources, config), this)
                                        }
                        }
                        .toMap()
                        .lift()
                        .map { newlyCompleted ->
                            newlyCompleted.mapKeys { it.key.name }
                                    .plus(newlyCompleted.mapKeys { it.key.implements }.minus(""))
                                    .plus(completedApplicationResources)
                        }
                        .flatMap { completedAfter -> buildApplicationResources(remainingAfter, completedAfter) }
                result.addHandler { log.info("finished  loading applicationResources: $it") }
                return result
        }

    private fun <R: ApplicationResource> ensureBothNamesRegistered(config: ApplicationResourceLoaderConfig, resourceLoader: ApplicationResourceLoader<R>): ApplicationResourceLoader<R> {
        ensureRegistered(config.name, resourceLoader)
        Option.invoke(config.implements).filter { it.isNotBlank() }.map { ensureRegistered(it, resourceLoader) }
        return resourceLoader
    }

    private fun  <R: ApplicationResource> ensureRegistered(alias: String, resourceLoader: ApplicationResourceLoader<R>): Try<Unit> =
            applicationRegistry.getLoader(alias)
                    .map { Unit }
                    .recoverWith { applicationRegistry.registerAlias(alias, resourceLoader) }

    private fun printFailureState(appConfig: AppConfig, throwable: Throwable) {
        log.info("Failed to start application", throwable)
        logAggregated(throwable)
        log.info("Generating failed application startup information...")
        val sb = StringBuilder()

        sb.appendln("=".repeat(20))
        sb.appendln("MISSING DEPENDENCY REPORT")
        sb.appendln("=".repeat(20))

        appConfig.applicationResourceLoaders
                .map { config ->
                    applicationRegistry
                            .getLoader(config.name)
                            .recoverWith { applicationRegistry.getLoader(config.implements) }
                            .map {
                                it.dependencies.filter { applicationRegistry.getLoader(config.applyOverride(it)).isFailure() }
                            }
                            .map {
                                if(it.isNotEmpty()) {
                                    sb.appendln("resource ${config.name}")
                                    sb.appendln("\timplements ${config.implements}")
                                    sb.appendln("\tmissing dependencies $it")
                                }
                            }
                }
                .liftTry()
                .fold({
                    log.error("failed to generate full missing dependency report", it)
                    sb.appendln("=".repeat(20))
                    sb.appendln(applicationRegistry)
                    sb.appendln("=".repeat(20))
                    log.info("\n$sb")
                }, {
                    sb.appendln("=".repeat(20))
                    sb.appendln(applicationRegistry)
                    sb.appendln("=".repeat(20))
                    log.info("\n$sb")
                })

    }

    private fun logAggregated(throwable: Throwable) {
        when(throwable) {
            is AggregateException ->
                throwable.flatten().errors.forEach {
                        log.error("Aggregated:", it)
                        logAggregated(it)
                    }
            else -> {}
        }
    }

}

sealed class AppLoadError: Throwable() {
        data class NoSuitableConstructor(val clazz: Class<*>): AppLoadError()
        data class MustExtendSkriptModule(val clazz: Class<*>): AppLoadError()
}

data class AppConfig(
        val modules: List<String>,
        val applicationResourceLoaders: List<ApplicationResourceLoaderConfig>)


val loadModulesIntoRegistry: Skript<AppConfig, Unit, SkriptApplicationLoader> = Skript.identity<AppConfig, SkriptApplicationLoader>()
        .map { it.modules }
        .mapTry { modules -> modules
                .map { Class.forName(it) }
                .map { clazz -> Try { clazz.getConstructor() }
                        .recoverWith { Try.Failure(AppLoadError.NoSuitableConstructor(clazz)) }
                        .map { it.newInstance() }
                        .filter { it is SkriptModule }
                        .recoverWith { Try.Failure(AppLoadError.MustExtendSkriptModule(clazz)) }
                        .map { it as SkriptModule }
                }
                .liftTry()
        }
        .mapTryWithTroupe { modules, troupe ->
            modules
                        .flatMap { it.loaders() }
                        .map(troupe.applicationRegistry::register)
                        .liftTry()
        }
        .map { Unit }

val loadApplication: Skript<String, SkriptApplication, SkriptApplicationLoader> = Skript.identity<String, SkriptApplicationLoader>()
        .map { FileReference.Relative(it) }
        .readFile()
        .map { it.readText().toByteArray() }
        .deserialize(AppConfig::class.java)
        .split(loadModulesIntoRegistry)
        .join { appConfig, _ -> appConfig }
        .flatMapWithTroupe { config, troupe -> troupe.buildApplication(config) }
