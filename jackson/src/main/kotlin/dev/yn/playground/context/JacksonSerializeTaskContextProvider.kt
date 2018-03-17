package dev.yn.playground.context

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.serialize.JacksonSerializeTaskExecutor

class JacksonSerializeTaskContextProvider(val objectMapper: ObjectMapper = defaultObjectMapper): SerializeTaskContextProvider<JacksonSerializeTaskExecutor> {
    companion object {
        val defaultObjectMapper by lazy {
            ObjectMapper()
                    .registerModule(KotlinModule())
                    .registerModule(JavaTimeModule())
        }
    }

    override fun getSerializeTaskExecutor(): AsyncResult<JacksonSerializeTaskExecutor> {
        return AsyncResult.succeeded(JacksonSerializeTaskExecutor(objectMapper))
    }
}

