package dev.yn.playground.serialize

import com.fasterxml.jackson.databind.ObjectMapper
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.result.toAsyncResult
import org.funktionale.tries.Try

class JacksonSerializeSkriptExecutor(val mapper: ObjectMapper): SerializeSkriptExecutor {

    
    override fun <T> serialize(command: SerializeCommand.Serialize<T>): AsyncResult<ByteArray> {
        return Try { mapper.writeValueAsBytes(command.value) }
                .toAsyncResult()
    }

    override fun <T> deserialize(command: SerializeCommand.Deserialize<T>): AsyncResult<T> {
        return Try { mapper.readValue(command.bytes, command.clazz) }
                .toAsyncResult()
    }
}

