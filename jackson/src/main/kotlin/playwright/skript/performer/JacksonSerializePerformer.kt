package playwright.skript.performer

import com.fasterxml.jackson.databind.ObjectMapper
import org.funktionale.tries.Try
import playwright.skript.result.AsyncResult
import playwright.skript.result.toAsyncResult

class JacksonSerializePerformer(val mapper: ObjectMapper): SerializePerformer {

    
    override fun <T> serialize(command: SerializeCommand.Serialize<T>): AsyncResult<ByteArray> {
        return Try { mapper.writeValueAsBytes(command.value) }
                .toAsyncResult()
    }

    override fun <T> deserialize(command: SerializeCommand.Deserialize<T>): AsyncResult<T> {
        return Try { mapper.readValue(command.bytes, command.clazz) }
                .toAsyncResult()
    }
}

