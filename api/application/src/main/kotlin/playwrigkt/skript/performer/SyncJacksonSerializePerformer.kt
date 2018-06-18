package playwrigkt.skript.performer

import com.fasterxml.jackson.databind.ObjectMapper
import arrow.core.Try
import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.result.AsyncResult

class SyncJacksonSerializePerformer(val mapper: ObjectMapper): SerializePerformer {
    override fun <T> serialize(command: SerializeCommand.Serialize<T>): AsyncResult<ByteArray> {
        return Try { mapper.writeValueAsBytes(command.value) }.toAsyncResult()
    }

    override fun <T> deserialize(command: SerializeCommand.Deserialize<T>): AsyncResult<T> {
        return Try { mapper.readValue(command.bytes, command.clazz) }.toAsyncResult()
    }
}

