package playwrigkt.skript.troupe

import playwrigkt.skript.performer.HttpRequestPerformer
import playwrigkt.skript.performer.PublishPerformer
import playwrigkt.skript.performer.SQLPerformer
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult

data class ApplicationTroupe(
        private val publishTroupe: QueuePublishTroupe,
        private val sqlTroupe: SQLTroupe,
        private val serializeTroupe: SerializeTroupe,
        private val httpRequestTroupe: HttpRequestTroupe):
        QueuePublishTroupe,
        SQLTroupe,
        SerializeTroupe,
        HttpRequestTroupe {
    override fun getHttpRequestPerformer(): AsyncResult<out HttpRequestPerformer> = httpRequestTroupe.getHttpRequestPerformer()
    override fun getPublishPerformer(): AsyncResult<out PublishPerformer<QueueMessage>> = publishTroupe.getPublishPerformer()
    override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = serializeTroupe.getSerializePerformer()
    override fun getSQLPerformer(): AsyncResult<out SQLPerformer> = sqlTroupe.getSQLPerformer()
}