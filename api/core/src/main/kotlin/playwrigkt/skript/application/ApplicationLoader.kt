package playwrigkt.skript.application

import org.funktionale.tries.Try
import org.slf4j.LoggerFactory
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.*
import playwrigkt.skript.file.FileReference
import playwrigkt.skript.performer.FilePerformer
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.FileTroupe
import playwrigkt.skript.troupe.SerializeTroupe

//TODO
/*
 * Loaders for all impl modules
 * Modules for all impl modules
 * Automatically find dependencies from single configured stage manager
 * load venues + produktions
 */
data class SkriptApplication(val applicationResources: Map<String, out ApplicationResource>)
data class SkriptApplicationLoader(val fileTroupe: FileTroupe, val serializeTroupe: SerializeTroupe, val applicationRegistry: ApplicationRegistry): FileTroupe, SerializeTroupe {
        val log = LoggerFactory.getLogger(this::class.java)

        override fun getFilePerformer(): AsyncResult<out FilePerformer> = fileTroupe.getFilePerformer()

        override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = serializeTroupe.getSerializePerformer()


        fun buildApplication(appConfig: AppConfig): AsyncResult<SkriptApplication> =
                buildStageManagers(appConfig.applicationResourceLoaders)
                        .map { SkriptApplication(it) }

        private fun buildStageManagers(remainingApplicationResources: List<ApplicationResourceLoaderConfig>,
                                       completedApplicationResources: Map<String, out ApplicationResource> = emptyMap()): AsyncResult<Map<String, out ApplicationResource>> {


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
                        .map { config -> config.name to
                                applicationRegistry.getLoader(config.name)
                                        .toAsyncResult()
                                        .flatMap {
                                                log.info("loading application resource $config with $it")
                                                it.loadResource.run(ApplicationResourceLoader.Input(completedApplicationResources, config), this)
                                        }
                        }
                        .toMap()
                        .lift()
                        .map { newlyCompleted -> newlyCompleted.plus(completedApplicationResources) }
                        .flatMap { completedAfter -> buildStageManagers(remainingAfter, completedAfter) }
                result.addHandler { log.info("finished  loading applicationResources: $it") }
                return result
        }
}

sealed class AppLoadError: Throwable() {
        data class NoSuitableConstructor(val clazz: Class<*>): AppLoadError()
        data class MustExtendSkriptModule(val clazz: Class<*>): AppLoadError()
        data class NoSuchStageManager(val name: String): AppLoadError()
        data class InvalidStageManager(val name: String, val instance: StageManager<*>): AppLoadError()
}

data class AppConfig(
        val modules: List<String>,
        val applicationResourceLoaders: List<ApplicationResourceLoaderConfig>)


val loadModules: Skript<AppConfig, Unit, SkriptApplicationLoader> = Skript.identity<AppConfig, SkriptApplicationLoader>()
        .mapTry { config ->
                config.modules
                        .map { Class.forName(it) }
                        .map { clazz -> Try { clazz.getConstructor() }
                                .rescue { Try.Failure(AppLoadError.NoSuitableConstructor(clazz)) }
                                .map { it.newInstance() }
                                .rescue { Try.Failure(AppLoadError.MustExtendSkriptModule(clazz)) }
                                .map { it as SkriptModule }
                        }
                        .lift()
        }
        .mapTryWithTroupe { modules, troupe ->
                modules
                        .flatMap { it.loaders() }
                        .map(troupe.applicationRegistry::register)
                        .lift()
        }
        .map { Unit }

val loadApplication: Skript<String, SkriptApplication, SkriptApplicationLoader> = Skript.identity<String, SkriptApplicationLoader>()
        .map { FileReference.Relative(it) }
        .readFile()
        .map { it.readText().toByteArray() }
        .deserialize(AppConfig::class.java)
        .split(loadModules)
        .join { appConfig, modules -> appConfig }
        .flatMapWithTroupe { config, troupe -> troupe.buildApplication(config) }