package playwrigkt.skript.application

import org.funktionale.tries.Try
import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.FileTroupe
import playwrigkt.skript.troupe.SyncFileTroupe

object SyncFileStageManagerLoader: StageManagerLoader<FileTroupe> {
    override fun register(registry: ApplicationRegistry): Try<Unit> = registry.register("file", this, emptyList())

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<FileTroupe>> =
        Try {
            object : StageManager<FileTroupe> {
                override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)

                override fun hireTroupe(): FileTroupe = SyncFileTroupe
            }
        }.toAsyncResult()
}