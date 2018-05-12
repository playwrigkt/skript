package playwrigkt.skript.application

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager

data class StageManagerLoaderConfig(val name: String,
                                    val dependencyOverrides: Map<String, String>,
                                    val config: ConfigValue = ConfigValue.Empty.Undefined) {
    fun applyOverride(dependency: String): String = dependencyOverrides.get(dependency)?:dependency
}

interface StageManagerLoader<Troupe> {
    data class Input(val existingManagers: Map<String, StageManager<*>>, val stageManagerLoaderConfig: StageManagerLoaderConfig)
    data class StageManagerException(val error: StageManagerError, override val cause: Throwable? = null): Exception(error.toString(), cause)
    sealed class StageManagerError {
        data class NoSuchManager(val name: String): StageManagerError()
    }

    val dependencies: List<String>
    val name: String
    val loadManager: Skript<Input, out StageManager<Troupe>, SkriptApplicationLoader>

    fun <Troupe> loadExistingStageManagerSkript(name: String): Skript<Input, StageManager<Troupe>, SkriptApplicationLoader> =
            Skript.mapTry {
                it.existingManagers.get(it.stageManagerLoaderConfig.applyOverride(name))
                        ?.let { Try { it as StageManager<Troupe> } }
                        ?:Try.Failure(StageManagerLoader.StageManagerException(StageManagerLoader.StageManagerError.NoSuchManager(name)))
            }
}