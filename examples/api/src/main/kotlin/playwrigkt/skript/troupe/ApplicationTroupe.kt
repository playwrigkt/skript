package playwrigkt.skript.troupe

import playwrigkt.skript.performer.HttpClientPerformer
import playwrigkt.skript.performer.QueuePublishPerformer
import playwrigkt.skript.performer.SqlPerformer
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.result.AsyncResult

data class ApplicationTroupe(
        private val publishTroupe: QueuePublishTroupe,
        private val sqlTroupe: SqlTroupe,
        private val serializeTroupe: SerializeTroupe,
        private val httpClientTroupe: HttpClientTroupe):
        QueuePublishTroupe,
        SqlTroupe,
        SerializeTroupe,
        HttpClientTroupe {
    override fun getHttpRequestPerformer(): AsyncResult<out HttpClientPerformer> = httpClientTroupe.getHttpRequestPerformer()
    override fun getPublishPerformer(): AsyncResult<out QueuePublishPerformer> = publishTroupe.getPublishPerformer()
    override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = serializeTroupe.getSerializePerformer()
    override fun getSQLPerformer(): AsyncResult<out SqlPerformer> = sqlTroupe.getSQLPerformer()
}