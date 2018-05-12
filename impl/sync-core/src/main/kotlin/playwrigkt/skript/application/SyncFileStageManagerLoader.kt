package playwrigkt.skript.application

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.FileTroupe
import playwrigkt.skript.troupe.SyncFileTroupe

object SyncFileStageManagerLoader: StageManagerLoader<FileTroupe> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "file"

    override val loadManager: Skript<StageManagerLoader.Input, StageManager<FileTroupe>, SkriptApplicationLoader> =
            Skript.map { stageManager }

    private val stageManager = object : StageManager<FileTroupe> {
        override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)

        override fun hireTroupe(): FileTroupe = SyncFileTroupe
    }
}