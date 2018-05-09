package playwrigkt.skript.application

import org.funktionale.option.getOrElse
import org.funktionale.option.toOption
import org.funktionale.tries.Try
import playwrigkt.skript.ex.lift
import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.LightweightSynchronized
import playwrigkt.skript.stagemanager.StageManager
import java.util.concurrent.locks.ReentrantLock


class ApplicationRegistry: LightweightSynchronized {
    private data class Registry(val loader: StageManagerLoader<*>, val dependencies: List<String>)

    data class RegistryException(val error: RegistryError, override val cause: Throwable? = null): Exception(error.toString())

    sealed class RegistryError {
        data class DuplicateStageManagerLoader(val name: String): RegistryError()
        data class NotFound(val name: String): RegistryError()
        data class UnsatisfiedDependencies(val remainingStageManagers: List<StageManagerLoaderConfig>,
                                           val completedStageManagers: Map<String, StageManager<*>>): RegistryError()
    }

    override fun toString(): String =
        """ApplicationRegistry(registry=$registry)"""
    override val lock: ReentrantLock = ReentrantLock()
    private val registry: MutableMap<String, StageManagerLoader<*>> = mutableMapOf()

    fun <Loader> register(loader: Loader): Try<Unit> where Loader: StageManagerLoader<*> = lock {
        registry.get(loader.name)
                ?.let { Try.Failure<Unit>(RegistryException(RegistryError.DuplicateStageManagerLoader(loader.name))) }
                ?: Try.Success(registry.put(loader.name, loader))
                        .map { Unit }

    }

    fun getLoader(name: String): Try<StageManagerLoader<*>> =
            registry.get(name)
                    ?.let { it }
                    ?.let { Try.Success(it) }
                    ?: Try.Failure(RegistryException(RegistryError.NotFound(name)))

    fun getDependencies(name: String): Try<List<String>> =
            registry.get(name)
                    ?.let { it.dependencies }
                    ?.let { Try.Success(it) }
                    ?: Try.Failure(RegistryException(RegistryError.NotFound(name)))

    fun buildStageManagers(remainingStageManagers: List<StageManagerLoaderConfig>,
                           completedStageManagers: Map<String, StageManager<*>> = emptyMap()): AsyncResult<Map<String, StageManager<*>>> {
        if(remainingStageManagers.isEmpty()) {
            return AsyncResult.succeeded(completedStageManagers)
        }

        val remainingAfter =
                remainingStageManagers.filterNot { config ->
                    dependenciesAreSatisfied(config, completedStageManagers)
                }

        if (remainingAfter.size.equals(remainingStageManagers.size)) {
            return AsyncResult.failed(RegistryException(RegistryError.UnsatisfiedDependencies(remainingAfter, completedStageManagers)))
        }

        return remainingStageManagers
                        .filter { config -> dependenciesAreSatisfied(config, completedStageManagers) }
                        .map { config -> config.name to
                                getLoader(config.name)
                                        .toAsyncResult()
                                        .flatMap {
                                            it.loadManager(completedStageManagers, config)
                                        }
                        }
                        .toMap()
                        .lift()
                        .map { newlyCompleted -> newlyCompleted.plus(completedStageManagers) }
                        .flatMap { completedAfter -> buildStageManagers(remainingAfter, completedAfter) }

    }

    private fun dependenciesAreSatisfied(config: StageManagerLoaderConfig,
                                         existingStageManagers: Map<String, StageManager<*>>): Boolean =
            existingStageManagers.keys.containsAll(
                    registry.get(config.name)
                            .toOption()
                            .map { registry ->
                                registry.dependencies.map { config.dependencyOverrides.get(it)?:it }
                            }
                            .getOrElse { emptyList() })
}