package playwrigkt.skript.troupe

import playwrigkt.skript.performer.FilePerformer
import playwrigkt.skript.performer.SyncFilePerformer
import playwrigkt.skript.result.AsyncResult

object SyncFileTroupe: FileTroupe {
    val performer by lazy { AsyncResult.succeeded(SyncFilePerformer) }
    override fun getFilePerformer(): AsyncResult<out FilePerformer> = performer
}