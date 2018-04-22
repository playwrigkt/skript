package playwrigkt.skript.performer

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import org.funktionale.tries.Try
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.ex.toAsyncResult

class VertxSerializePerformer(val objectMapper: ObjectMapper): SerializePerformer {
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