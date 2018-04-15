package playwrigkt.skript.troupe

import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.result.AsyncResult

interface SerializeTroupe {
    fun getSerializePerformer(): AsyncResult<out SerializePerformer>
}