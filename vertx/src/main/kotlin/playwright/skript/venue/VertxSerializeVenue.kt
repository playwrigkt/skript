package playwright.skript.venue

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import playwright.skript.performer.SerializePerformer
import playwright.skript.performer.VertxSerializePerformer
import playwright.skript.result.AsyncResult

class VertxSerializeVenue(val objectMapper: ObjectMapper? = null): Venue<SerializePerformer> {
    companion object {
        val defaultObjectMapper by lazy {
            ObjectMapper()
                    .registerModule(KotlinModule())
                    .registerModule(JavaTimeModule())
        }
    }

    override fun provideStage(): AsyncResult<SerializePerformer> =
            AsyncResult.succeeded(VertxSerializePerformer(objectMapper ?: defaultObjectMapper))
}