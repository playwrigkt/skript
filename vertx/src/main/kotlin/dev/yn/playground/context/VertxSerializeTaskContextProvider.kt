package dev.yn.playground.context

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.serialize.VertxSerializeTaskExecutor

class VertxSerializeTaskContextProvider(val objectMapper: ObjectMapper? = null): SerializeTaskContextProvider<VertxSerializeTaskExecutor> {
    companion object {
        val defaultObjectMapper by lazy {
            ObjectMapper()
                    .registerModule(KotlinModule())
                    .registerModule(JavaTimeModule())
        }
    }

    override fun getSerializeTaskExecutor(): AsyncResult<VertxSerializeTaskExecutor> {
        return AsyncResult.succeeded(VertxSerializeTaskExecutor(objectMapper?: defaultObjectMapper))
    }

}