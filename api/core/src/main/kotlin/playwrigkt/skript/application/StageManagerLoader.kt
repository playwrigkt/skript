package playwrigkt.skript.application

import org.funktionale.tries.Try
import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager

data class StageManagerLoaderConfig(val name: String,
                                    val dependencyOverrides: Map<String, String>,
                                    val config: ConfigValue = ConfigValue.Empty.Undefined) {
    fun applyOverride(dependency: String): String = dependencyOverrides.get(dependency)?:dependency
}

interface StageManagerLoader<Troupe> {
    data class StageManagerException(val error: StageManagerError, override val cause: Throwable? = null): Exception(error.toString(), cause)
    sealed class StageManagerError {
        data class NoSuchManager(val name: String): StageManagerError()
    }

    val dependencies: List<String>
    val name: String

    fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<Troupe>>

    fun <Troupe> loadExisting(name: String, existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): Try<StageManager<Troupe>> =
            existingManagers.get(config.applyOverride(name))
                    ?.let { Try { it as StageManager<Troupe> } }
                    ?:Try.Failure(StageManagerLoader.StageManagerException(StageManagerLoader.StageManagerError.NoSuchManager(name)))
}