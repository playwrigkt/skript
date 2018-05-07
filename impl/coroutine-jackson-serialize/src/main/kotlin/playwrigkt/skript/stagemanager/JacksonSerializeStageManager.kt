package playwrigkt.skript.stagemanager

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import playwrigkt.skript.jackson.SkriptJacksonObjectMapper
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.JacksonSerializeTroupe
import playwrigkt.skript.troupe.SerializeTroupe

data class JacksonSerializeStageManager(val objectMapper: ObjectMapper = defaultObjectMapper): StageManager<SerializeTroupe> {
    companion object {
        val defaultObjectMapper by lazy {
            SkriptJacksonObjectMapper.configure(ObjectMapper())
        }
    }

    override fun hireTroupe(): SerializeTroupe = JacksonSerializeTroupe(objectMapper)

    override fun tearDown(): AsyncResult<Unit> {
        return AsyncResult.succeeded(Unit)
    }
}
