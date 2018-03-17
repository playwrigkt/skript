package dev.yn.playground.context

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.serialize.JacksonSerializeSkriptExecutor

class JacksonSerializeSkriptContextProvider(val objectMapper: ObjectMapper = defaultObjectMapper): SerializeSkriptContextProvider<JacksonSerializeSkriptExecutor> {
    companion object {
        val defaultObjectMapper by lazy {
            ObjectMapper()
                    .registerModule(KotlinModule())
                    .registerModule(JavaTimeModule())
        }
    }

    override fun getSerializeSkriptExecutor(): AsyncResult<JacksonSerializeSkriptExecutor> {
        return AsyncResult.succeeded(JacksonSerializeSkriptExecutor(objectMapper))
    }
}
