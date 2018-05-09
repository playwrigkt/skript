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
    override val dependencies: List<String> = listOf("serialize", "file", "inputStream", "outputStream")
    override val name: String = "exampleApp"

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
}

object StdInStageManagerLoader: StageManagerLoader<InputStreamTroupe> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "stdIn"

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<InputStreamTroupe>> =
            Try {
                object : StageManager<InputStreamTroupe> {
                    override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
                    override fun hireTroupe(): InputStreamTroupe = CoroutineInputStreamTroupe(System.`in`)
                }
            }.toAsyncResult()
}

object StdOutStageManagerLoader: StageManagerLoader<OutputStreamTroupe> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "stdOut"

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<OutputStreamTroupe>> =
            Try {
                object : StageManager<OutputStreamTroupe> {
                    override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
                    override fun hireTroupe(): OutputStreamTroupe = CoroutineOutputStreamTroupe(System.`out`)
                }
            }.toAsyncResult()
}

