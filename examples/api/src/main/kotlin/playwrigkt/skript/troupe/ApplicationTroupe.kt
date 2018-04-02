package playwrigkt.skript.troupe

import playwright.skript.queue.QueueMessage
import playwright.skript.troupe.QueuePublishTroupe
import playwrigkt.skript.performer.PublishPerformer
import playwrigkt.skript.performer.SQLPerformer
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.result.AsyncResult

data class ApplicationTroupe(
        private val publishTroupe: QueuePublishTroupe,
        private val sqlTroupe: SQLTroupe,
        private val serializeTroupe: SerializeTroupe):
        QueuePublishTroupe,
        SQLTroupe,
        SerializeTroupe {
    override fun getPublishPerformer(): AsyncResult<out PublishPerformer<QueueMessage>> = publishTroupe.getPublishPerformer()
    override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = serializeTroupe.getSerializePerformer()
    override fun getSQLPerformer(): AsyncResult<out SQLPerformer> = sqlTroupe.getSQLPerformer()
}