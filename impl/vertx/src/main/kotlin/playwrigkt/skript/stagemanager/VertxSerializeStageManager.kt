package playwrigkt.skript.stagemanager

import com.fasterxml.jackson.databind.ObjectMapper
import playwrigkt.skript.jackson.SkriptJacksonObjectMapper
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SerializeTroupe
import playwrigkt.skript.troupe.VertxSerializeTroupe

data class VertxSerializeStageManager(val objectMapper: ObjectMapper? = null): StageManager<SerializeTroupe> {
    companion object {
        val defaultObjectMapper by lazy {
            SkriptJacksonObjectMapper.configure(ObjectMapper())
        }
    }

    override fun hireTroupe(): SerializeTroupe = VertxSerializeTroupe(objectMapper
            ?: defaultObjectMapper)

    override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
}
