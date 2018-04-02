package playwrigkt.skript.stagemanager

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import playwrigkt.skript.performer.VertxSerializePerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SerializeTroupe

data class VertxSerializeStageManager(val objectMapper: ObjectMapper? = null): StageManager<SerializeTroupe> {
    companion object {
        val defaultObjectMapper by lazy {
            ObjectMapper()
                    .registerModule(KotlinModule())
                    .registerModule(JavaTimeModule())
        }
    }

    override fun hireTroupe(): SerializeTroupe =
            object :SerializeTroupe {
                val performer: AsyncResult<VertxSerializePerformer> by lazy {
                    AsyncResult.succeeded(VertxSerializePerformer(objectMapper ?: defaultObjectMapper))
                }

                override fun getSerializePerformer(): AsyncResult<VertxSerializePerformer> = performer.copy()

            }
}