package playwrigkt.skript.troupe

import com.fasterxml.jackson.databind.ObjectMapper
import playwrigkt.skript.performer.VertxSerializePerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.VertxSerializeStageManager

data class VertxSerializeTroupe(val objectMapper: ObjectMapper): SerializeTroupe {
    val performer: AsyncResult<VertxSerializePerformer> by lazy {
        AsyncResult.succeeded(VertxSerializePerformer(objectMapper))
    }

    override fun getSerializePerformer(): AsyncResult<VertxSerializePerformer> = performer
}