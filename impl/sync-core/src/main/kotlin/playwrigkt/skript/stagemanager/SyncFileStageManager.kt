package playwrigkt.skript.stagemanager

import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.FileTroupe
import playwrigkt.skript.troupe.SyncFileTroupe

object SyncFileStageManager: StageManager<FileTroupe> {
    override fun hireTroupe(): FileTroupe = SyncFileTroupe

    override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
}