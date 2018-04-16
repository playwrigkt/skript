package playwrigkt.skript.queue

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.QueuePublishTroupe

sealed class QueuePublishSkript: Skript<QueueMessage, Unit, QueuePublishTroupe> {

    companion object {
        fun publish(): QueuePublishSkript {
            return Publish()
        }
    }

    class Publish(): QueuePublishSkript() {
        override fun run(i: QueueMessage, troupe: QueuePublishTroupe): AsyncResult<Unit> {
            return troupe.getPublishPerformer()
                    .flatMap { publishPerformer -> publishPerformer.publish(i) }
        }
    }
}