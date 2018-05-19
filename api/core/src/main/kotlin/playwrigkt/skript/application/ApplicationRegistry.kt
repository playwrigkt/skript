package playwrigkt.skript.application

import org.funktionale.option.getOrElse
import org.funktionale.option.toOption
import org.funktionale.tries.Try
import playwrigkt.skript.result.LightweightSynchronized
import java.util.concurrent.locks.ReentrantLock


class ApplicationRegistry: LightweightSynchronized {
    data class RegistryException(val error: RegistryError, override val cause: Throwable? = null): Exception(error.toString())

    sealed class RegistryError {
        data class DuplicateStageManagerLoader(val name: String): RegistryError()
        data class NotFound(val name: String): RegistryError()
        data class UnsatisfiedDependencies(val remainingApplicationResources: List<ApplicationResourceLoaderConfig>,
                                           val completedApplicationResources: Map<String, *>): RegistryError()
    }

    override fun toString(): String =
        """ApplicationRegistry(registry=$registry)"""
    override val lock: ReentrantLock = ReentrantLock()
    private val registry: MutableMap<String, ApplicationResourceLoader<out ApplicationResource>> = mutableMapOf()

    fun <Loader> register(loader: Loader): Try<Unit> where Loader: ApplicationResourceLoader<out ApplicationResource> = lock {
        registry.get(loader.name())
                ?.let { Try.Failure<Unit>(RegistryException(RegistryError.DuplicateStageManagerLoader(loader.name()))) }
                ?: Try.Success(registry.put(loader.name(), loader))
                        .map { Unit }

    }

    fun getLoader(name: String): Try<ApplicationResourceLoader<*>> =
            registry.get(name)
                    ?.let { it }
                    ?.let { Try.Success(it) }
                    ?: Try.Failure(RegistryException(RegistryError.NotFound(name)))

    fun getDependencies(name: String): Try<List<String>> =
            registry.get(name)
                    ?.let { it.dependencies }
                    ?.let { Try.Success(it) }
                    ?: Try.Failure(RegistryException(RegistryError.NotFound(name)))

    fun dependenciesAreSatisfied(config: ApplicationResourceLoaderConfig,
                                 existingStageManagers: Map<String, ApplicationResource>): Boolean =
            existingStageManagers.keys.containsAll(
                    registry.get(config.name)
                            .toOption()
                            .map { registry ->
                                registry.dependencies.map { config.dependencyOverrides.get(it)?:it }
                            }
                            .getOrElse { emptyList() })
}