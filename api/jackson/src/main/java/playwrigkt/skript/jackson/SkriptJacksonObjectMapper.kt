package playwrigkt.skript.jackson

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

object SkriptJacksonObjectMapper {
    fun instance(): ObjectMapper = configure(ObjectMapper())
    fun configure(objectMapper: ObjectMapper): ObjectMapper =
        objectMapper
                .let(ConfigValueJacksonJsonDeserializer()::register)
                .registerModule(KotlinModule())
                .registerModule(JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, false)
}