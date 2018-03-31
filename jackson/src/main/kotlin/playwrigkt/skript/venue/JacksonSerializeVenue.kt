package playwrigkt.skript.venue

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import playwrigkt.skript.performer.JacksonSerializePerformer
import playwrigkt.skript.result.AsyncResult

class JacksonSerializeVenue(val objectMapper: ObjectMapper = defaultObjectMapper): Venue<JacksonSerializePerformer> {
    companion object {
        val defaultObjectMapper by lazy {
            ObjectMapper()
                    .registerModule(KotlinModule())
                    .registerModule(JavaTimeModule())
        }
    }

    override fun provideStage(): AsyncResult<JacksonSerializePerformer> {
        return AsyncResult.succeeded(JacksonSerializePerformer(objectMapper))
    }
}

