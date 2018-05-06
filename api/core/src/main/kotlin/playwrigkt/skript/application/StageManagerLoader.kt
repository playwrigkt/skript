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
    fun register(registry: ApplicationRegistry): Try<Unit>
    fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<Troupe>>
}