package playwrigkt.skript.troupe

import playwrigkt.skript.performer.QueuePublishPerformer
import playwrigkt.skript.result.AsyncResult

interface QueuePublishTroupe {
    fun getPublishPerformer(): AsyncResult<out QueuePublishPerformer>
}