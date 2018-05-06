package playwrigkt.skript.example

import org.funktionale.tries.Try
import playwrigkt.skript.application.ApplicationRegistry
import playwrigkt.skript.application.StageManagerLoader
import playwrigkt.skript.application.StageManagerLoaderConfig
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.iostream.CoroutineInputStreamTroupe
import playwrigkt.skript.iostream.CoroutineOutputStreamTroupe
import playwrigkt.skript.iostream.InputStreamTroupe
import playwrigkt.skript.iostream.OutputStreamTroupe
import playwrigkt.skript.troupe.FileTroupe
import playwrigkt.skript.troupe.SerializeTroupe

object MyStageManagerTroupeLoader: StageManagerLoader<MyTroupe> {
    override fun register(registry: ApplicationRegistry): Try<Unit> = registry.register("exampleApp", this, listOf("serialize", "file", "inputStream", "outputStream"))

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<MyStageManager> =
        loadExisting<SerializeTroupe>("serialize", existingManagers, config).flatMap { serializeStageManager ->
            loadExisting<FileTroupe>("file", existingManagers, config).flatMap { fileStageManager ->
                loadExisting<InputStreamTroupe>("inputStream", existingManagers, config).flatMap { inputStream ->
                    loadExisting<OutputStreamTroupe>("outputStream", existingManagers, config).map { outputStream ->
                        MyStageManager(serializeStageManager, fileStageManager, inputStream, outputStream)
                    }
                }
            }
        }.toAsyncResult()

    private fun <Troupe> loadExisting(name: String, existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): Try<StageManager<Troupe>> =
        existingManagers.get(config.applyOverride(name))
                ?.let { Try { it as StageManager<Troupe> } }
                ?:Try.Failure(StageManagerLoader.StageManagerException(StageManagerLoader.StageManagerError.NoSuchManager(name)))
}

object StdInStageManagerLoader: StageManagerLoader<InputStreamTroupe> {
    override fun register(registry: ApplicationRegistry): Try<Unit> = registry.register("stdIn", this, emptyList())

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<InputStreamTroupe>> =
            Try {
                object : StageManager<InputStreamTroupe> {
                    override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
                    override fun hireTroupe(): InputStreamTroupe = CoroutineInputStreamTroupe(System.`in`)
                }
            }.toAsyncResult()
}

object StdOutStageManagerLoader: StageManagerLoader<OutputStreamTroupe> {
    override fun register(registry: ApplicationRegistry): Try<Unit> = registry.register("stdOut", this, emptyList())

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<OutputStreamTroupe>> =
            Try {
                object : StageManager<OutputStreamTroupe> {
                    override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
                    override fun hireTroupe(): OutputStreamTroupe = CoroutineOutputStreamTroupe(System.`out`)
                }
            }.toAsyncResult()
}

