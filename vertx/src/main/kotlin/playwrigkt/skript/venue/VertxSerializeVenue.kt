package playwrigkt.skript.venue

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.performer.VertxSerializePerformer
import playwrigkt.skript.result.AsyncResult

class VertxSerializeVenue(val objectMapper: ObjectMapper? = null): Venue<VertxSerializePerformer> {
    companion object {
        val defaultObjectMapper by lazy {
            ObjectMapper()
                    .registerModule(KotlinModule())
                    .registerModule(JavaTimeModule())
        }
    }

    override fun provideStage(): AsyncResult<VertxSerializePerformer> =
            AsyncResult.succeeded(VertxSerializePerformer(objectMapper ?: defaultObjectMapper))
}