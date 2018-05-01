package playwrigkt.skript.config

import com.fasterxml.jackson.databind.JsonNode
import org.funktionale.option.getOrElse
import org.funktionale.option.toOption
import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.file.*
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.ex.deserialize
import playwrigkt.skript.ex.lift
import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.result.LightweightSynchronized
import playwrigkt.skript.stagemanager.JacksonSerializeStageManager
import playwrigkt.skript.troupe.SerializeTroupe
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.locks.ReentrantLock

data class AppLoader(val fileTroupe: FileTroupe, val serializeTroupe: SerializeTroupe, val configTroupe: ConfigTroupe): FileTroupe, SerializeTroupe {
    override fun getFilePerformer(): AsyncResult<FilePerformer> = fileTroupe.getFilePerformer()

    override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = serializeTroupe.getSerializePerformer()
}



val loadStageManagers = Skript.identity<String, AppLoader>()
        .map { FileReference.Relative(it) }
        .readFile()
        .map { it.readText().toByteArray() }
        .deserialize(AppConfig::class.java)
        .map { ApplicationRegistry.default.buildStageManagers(it.stageManagerLoaders) }



class ApplicationRegistry: LightweightSynchronized {
    companion object {
        val default by lazy { ApplicationRegistry() }
    }

    data class Registry(val loader: StageManagerLoader<*>, val dependencies: List<String>)

    data class RegistryException(val error: RegistryError, override val cause: Throwable? = null): Exception(error.toString())

    sealed class RegistryError {
        data class DuplicateStageManagerLoader(val name: String): RegistryError()
        data class NotFound(val name: String): RegistryError()
        data class UnsatisfiedDependencies(val remainingStageManagers: List<StageManagerLoaderConfig>,
                                           val completedStageManagers: Map<String, StageManager<*>>): RegistryError()
    }

    override val lock: ReentrantLock = ReentrantLock()
    private val stageManagerLoaders: MutableMap<String, StageManagerLoader<*>> = mutableMapOf()
    private val stageManagerDependencies: MutableMap<String, List<String>> = mutableMapOf()

    fun <Loader> register(name: String, loader: Loader, dependencies: List<String>): Try<Unit> where Loader: StageManagerLoader<*> = lock {
        stageManagerDependencies.get(name)?.let { stageManagerLoaders.get(name) }
                ?.let { Try.Failure<Unit>(RegistryException(RegistryError.DuplicateStageManagerLoader(name))) }
                ?: Try.Success(stageManagerLoaders.put(name, loader))
                        .map { stageManagerDependencies.put(name, dependencies) }
                        .map { Unit }

    }

    fun getLoader(name: String):  Try<StageManagerLoader<*>> =
            stageManagerLoaders.get(name)
                    ?.let { Try.Success(it) }
                    ?: Try.Failure(RegistryException(RegistryError.NotFound(name)))

    fun getDependencies(name: String): Try<List<String>> =
            stageManagerDependencies.get(name)
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

        val completedAfter =
                remainingStageManagers
                        .filter { config -> dependenciesAreSatisfied(config, completedStageManagers) }
                        .map { config ->
                            println("building $config")
                            config.name to
                                    getLoader(config.name)
                                            .toAsyncResult()
                                            .flatMap {
                                                println("loading: $completedStageManagers\nand:$config")
                                                it.loadManager(completedStageManagers, config) }
                        }
                        .toMap()
                        .lift()
                        .map { it.plus(completedStageManagers) }

        if (remainingAfter.size.equals(remainingStageManagers.size)) {
            return AsyncResult.failed(RegistryException(RegistryError.UnsatisfiedDependencies(remainingAfter, completedStageManagers)))
        }

        return completedAfter.flatMap {
            buildStageManagers(remainingAfter, it)
        }
    }

    private fun dependenciesAreSatisfied(config: StageManagerLoaderConfig,
                                         existingStageManagers: Map<String, StageManager<*>>): Boolean =
            existingStageManagers.keys.containsAll(
                    stageManagerDependencies.get(config.name)
                            .toOption()
                            .map { registryDependencies ->
                                registryDependencies.map { config.dependencyOverrides.get(it)?:it }
                            }
                            .getOrElse { emptyList() })
}

data class AppConfig(val stageManagerLoaders: List<StageManagerLoaderConfig>)

data class StageManagerLoaderConfig(val name: String, val dependencyOverrides: Map<String, String>, val config: ConfigValue) {
    fun applyOverride(dependency: String): String = dependencyOverrides.get(dependency)?:dependency
}

interface StageManagerLoader<Troupe> {
    data class StageManagerException(val error: StageManagerError, override val cause: Throwable? = null): Exception(error.toString(), cause)
    sealed class StageManagerError {
        data class NoSuchManager(val name: String): StageManagerError()
    }
    fun register(registry: ApplicationRegistry): Try<Unit> = registry.register("exampleApp", this, listOf("serialize", "file", "ioStream"))
    fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<Troupe>>
}


object MyStageManagerTroupeLoader: StageManagerLoader<MyTroupe> {
    init {
        register(ApplicationRegistry.default)
    }

    override fun register(registry: ApplicationRegistry): Try<Unit> = registry.register("exampleApp", this, listOf("serialize", "file", "ioStream"))

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<MyStageManager> =
        loadExisting<SerializeTroupe>("serialize", existingManagers, config).flatMap { serializeStageManager ->
            loadExisting<FileTroupe>("file", existingManagers, config).flatMap { fileStageManager ->
                loadExisting<IOStreamTroupe>("ioStream", existingManagers, config).map { ioStreamManager ->
                    MyStageManager(serializeStageManager, fileStageManager, ioStreamManager)
                }
            }
        }.toAsyncResult()

    private fun <Troupe> loadExisting(name: String, existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): Try<StageManager<Troupe>> =
        existingManagers.get(config.applyOverride(name))
                ?.let { Try { it as StageManager<Troupe> } }
                ?:Try.Failure(StageManagerLoader.StageManagerException(StageManagerLoader.StageManagerError.NoSuchManager(name)))
}



object FileStageManagerLoader: StageManagerLoader<FileTroupe> {
    init {
        register(ApplicationRegistry.default)
    }

    override fun register(registry: ApplicationRegistry): Try<Unit> = registry.register("file", this, emptyList())

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<FileTroupe>> =
        Try {
            object : StageManager<FileTroupe> {
                override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)

                override fun hireTroupe(): FileTroupe = FileTroupe()
            }
        }.toAsyncResult()
}

object IOStreamStageManagerLoader: StageManagerLoader<IOStreamTroupe> {
    init {
        register(ApplicationRegistry.default)
    }

    override fun register(registry: ApplicationRegistry): Try<Unit> = registry.register("ioStream", this, emptyList())

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<IOStreamTroupe>> =
            Try {
                object : StageManager<IOStreamTroupe> {
                    override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)

                    //TODO IOStream config
                    override fun hireTroupe(): IOStreamTroupe = IOStreamTroupe(IOStream(BufferedReader(InputStreamReader(System.`in`)), BufferedWriter(OutputStreamWriter(System.`out`))))
                }
            }.toAsyncResult()
}

object JacksonSerializeStageManagerLoader: StageManagerLoader<SerializeTroupe> {
    init {
        register(ApplicationRegistry.default)
    }

    override fun register(registry: ApplicationRegistry): Try<Unit> = registry.register("serialize", this, emptyList())

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<SerializeTroupe>> =
            Try { JacksonSerializeStageManager() }.toAsyncResult()
}