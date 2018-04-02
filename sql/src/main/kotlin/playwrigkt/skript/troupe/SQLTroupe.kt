package playwrigkt.skript.troupe

import playwrigkt.skript.performer.SQLPerformer
import playwrigkt.skript.result.AsyncResult

interface SQLTroupe {
    fun getSQLPerformer(): AsyncResult<out SQLPerformer>
}