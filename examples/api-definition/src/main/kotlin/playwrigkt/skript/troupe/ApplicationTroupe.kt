package playwrigkt.skript.troupe

import playwrigkt.skript.performer.*
import playwrigkt.skript.result.AsyncResult

data class ApplicationTroupe(
        private val publishTroupe: QueuePublishTroupe,
        private val sqlTroupe: SqlTroupe,
        private val serializeTroupe: SerializeTroupe,
        private val httpClientTroupe: HttpClientTroupe,
        private val configTroupe: ConfigTroupe):
        QueuePublishTroupe,
        SqlTroupe,
        SerializeTroupe,
        HttpClientTroupe,
        ConfigTroupe {
    override fun getConfigPerformer(): AsyncResult<out ConfigPerformer> = configTroupe.getConfigPerformer()
    override fun getHttpRequestPerformer(): AsyncResult<out HttpClientPerformer> = httpClientTroupe.getHttpRequestPerformer()
    override fun getPublishPerformer(): AsyncResult<out QueuePublishPerformer> = publishTroupe.getPublishPerformer()
    override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = serializeTroupe.getSerializePerformer()
    override fun getSQLPerformer(): AsyncResult<out SqlPerformer> = sqlTroupe.getSQLPerformer()
}