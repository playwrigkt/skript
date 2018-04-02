package playwrigkt.skript.stagemanager

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import playwrigkt.skript.performer.JacksonSerializePerformer
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SerializeTroupe

data class JacksonSerializeStageManager(val objectMapper: ObjectMapper = defaultObjectMapper): StageManager<SerializeTroupe> {
    companion object {
        val defaultObjectMapper by lazy {
            ObjectMapper()
                    .registerModule(KotlinModule())
                    .registerModule(JavaTimeModule())
        }
    }

    override fun hireTroupe(): SerializeTroupe =
        object: SerializeTroupe {
            val perfomer: AsyncResult<JacksonSerializePerformer> by lazy {
                AsyncResult.succeeded(JacksonSerializePerformer(objectMapper))
            }

            override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = perfomer.copy()
        }
}

