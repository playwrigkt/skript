package playwrigkt.skript.stagemanager

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import playwrigkt.skript.troupe.SerializeTroupe
import playwrigkt.skript.troupe.VertxSerializeTroupe

data class VertxSerializeStageManager(val objectMapper: ObjectMapper? = null): StageManager<SerializeTroupe> {
    companion object {
        val defaultObjectMapper by lazy {
            ObjectMapper()
                    .registerModule(KotlinModule())
                    .registerModule(JavaTimeModule())
        }
    }

    override fun hireTroupe(): SerializeTroupe = VertxSerializeTroupe(objectMapper
            ?: defaultObjectMapper)
}
