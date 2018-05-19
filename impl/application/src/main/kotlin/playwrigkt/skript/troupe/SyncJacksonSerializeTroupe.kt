package playwrigkt.skript.troupe

import com.fasterxml.jackson.databind.ObjectMapper
import playwrigkt.skript.performer.SyncJacksonSerializePerformer
import playwrigkt.skript.result.AsyncResult

data class SyncJacksonSerializeTroupe(val objectMapper: ObjectMapper): SerializeTroupe {
    val perfomer: AsyncResult<SyncJacksonSerializePerformer> by lazy {
        AsyncResult.succeeded(SyncJacksonSerializePerformer(objectMapper))
    }

    override fun getSerializePerformer(): AsyncResult<SyncJacksonSerializePerformer> = perfomer
}
