package playwrigkt.skript.application

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.*
import playwrigkt.skript.file.*
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

data class AppLoader(val fileTroupe: FileTroupe, val serializeTroupe: SerializeTroupe, val applicationRegistry: ApplicationRegistry): FileTroupe, SerializeTroupe {
        override fun getFilePerformer(): AsyncResult<out FilePerformer> = fileTroupe.getFilePerformer()

        override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = serializeTroupe.getSerializePerformer()
}

sealed class AppLoadError: Throwable() {
        data class NoSuitableConstructor(val clazz: Class<*>): AppLoadError()
        data class MustExtendSkriptModule(val clazz: Class<*>): AppLoadError()
}

data class AppConfig(
        val modules: List<String>,
        val stageManagerLoaders: List<StageManagerLoaderConfig>)


val loadModules: Skript<AppConfig, Unit, AppLoader> = Skript.identity<AppConfig, AppLoader>()
        .mapTry { config ->
                config.modules
                        .map { Class.forName(it) }
                        .map { clazz -> Try { clazz.getConstructor() }
                                .rescue { Try.Failure(AppLoadError.NoSuitableConstructor(clazz)) }
                                .map { it.newInstance() }
                                .rescue { Try.Failure(AppLoadError.MustExtendSkriptModule(clazz)) }
                                .map { it as SkriptModule }
                }.lift()
        }
        .mapTryWithTroupe { modules, troupe ->
                modules
                        .map { Try {
                                it.loaders().map { it.register(troupe.applicationRegistry) }
                        } }
                        .lift()
        }
        .map { Unit }

val loadStageManagers: Skript<AppConfig, Map<String, StageManager<*>>, AppLoader> =
        Skript.identity<AppConfig, AppLoader>()
                .flatMapWithTroupe { config, troupe -> troupe.applicationRegistry.buildStageManagers(config.stageManagerLoaders) }

val loadApplication: Skript<String, Map<String, StageManager<*>>, AppLoader> = Skript.identity<String, AppLoader>()
        .map { FileReference.Relative(it) }
        .readFile()
        .map { it.readText().toByteArray() }
        .deserialize(AppConfig::class.java)
        .split(loadModules)
        .join { appConfig, modules -> appConfig }
        .andThen(loadStageManagers)