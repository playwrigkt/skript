package playwrigkt.skript.troupe

import playwrigkt.skript.performer.SqlPerformer
import playwrigkt.skript.result.AsyncResult

interface SqlTroupe {
    fun getSQLPerformer(): AsyncResult<out SqlPerformer>
}