package dev.yn.playground.serialize

import com.fasterxml.jackson.databind.ObjectMapper
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.result.toAsyncResult
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import org.funktionale.tries.Try

class VertxSerializeSkriptExecutor(val objectMapper: ObjectMapper): SerializeSkriptExecutor {
    init {
        Json.mapper = objectMapper
    }
    override fun <T> serialize(command: SerializeCommand.Serialize<T>): AsyncResult<ByteArray> {
        return Try { Json.encode(command.value).toByteArray() }
                .toAsyncResult()
    }

    override fun <T> deserialize(command: SerializeCommand.Deserialize<T>): AsyncResult<T> {
        return Try { Json.decodeValue(Buffer.buffer(command.bytes), command.clazz) }
                .toAsyncResult()
    }

}