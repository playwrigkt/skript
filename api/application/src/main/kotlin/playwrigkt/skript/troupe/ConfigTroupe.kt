package playwrigkt.skript.troupe

import playwrigkt.skript.performer.ConfigPerformer
import playwrigkt.skript.result.AsyncResult

interface ConfigTroupe {
    fun getConfigPerformer(): AsyncResult<out ConfigPerformer>
}