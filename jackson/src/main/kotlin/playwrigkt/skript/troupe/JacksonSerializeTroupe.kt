package playwrigkt.skript.troupe

import com.fasterxml.jackson.databind.ObjectMapper
import playwrigkt.skript.performer.JacksonSerializePerformer
import playwrigkt.skript.result.AsyncResult

data class JacksonSerializeTroupe(val objectMapper: ObjectMapper): SerializeTroupe {
    val perfomer: AsyncResult<JacksonSerializePerformer> by lazy {
        AsyncResult.succeeded(JacksonSerializePerformer(objectMapper))
    }

    override fun getSerializePerformer(): AsyncResult<JacksonSerializePerformer> = perfomer.copy()
}
