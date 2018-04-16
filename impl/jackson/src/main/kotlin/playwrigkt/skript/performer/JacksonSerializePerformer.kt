package playwrigkt.skript.performer

import com.fasterxml.jackson.databind.ObjectMapper
import org.funktionale.tries.Try
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult

class JacksonSerializePerformer(val mapper: ObjectMapper): SerializePerformer {

    
    override fun <T> serialize(command: SerializeCommand.Serialize<T>): AsyncResult<ByteArray> {
        return runAsync { mapper.writeValueAsBytes(command.value) }
    }

    override fun <T> deserialize(command: SerializeCommand.Deserialize<T>): AsyncResult<T> {
        return runAsync { mapper.readValue(command.bytes, command.clazz) }
    }
}

