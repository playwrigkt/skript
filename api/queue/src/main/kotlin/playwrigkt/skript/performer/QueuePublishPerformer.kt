package playwrigkt.skript.performer

import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult

interface QueuePublishPerformer {
    fun publish(command: QueueMessage): AsyncResult<Unit>
}