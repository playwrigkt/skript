package playwrigkt.skript.troupe

import playwrigkt.skript.performer.FilePerformer
import playwrigkt.skript.result.AsyncResult

interface FileTroupe {
    fun getFilePerformer(): AsyncResult<out FilePerformer>
}