package playwrigkt.skript.troupe

import playwrigkt.skript.performer.*
import playwrigkt.skript.result.AsyncResult

data class ApplicationTroupe(
        private val publishTroupe: QueuePublishTroupe,
        private val sqlTroupe: SQLTroupe,
        private val serializeTroupe: SerializeTroupe,
        private val httpClientTroupe: HttpClientTroupe):
        QueuePublishTroupe,
        SQLTroupe,
        SerializeTroupe,
        HttpClientTroupe {
    override fun getHttpRequestPerformer(): AsyncResult<out HttpClientPerformer> = httpClientTroupe.getHttpRequestPerformer()
    override fun getPublishPerformer(): AsyncResult<out QueuePublishPerformer> = publishTroupe.getPublishPerformer()
    override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = serializeTroupe.getSerializePerformer()
    override fun getSQLPerformer(): AsyncResult<out SQLPerformer> = sqlTroupe.getSQLPerformer()
}