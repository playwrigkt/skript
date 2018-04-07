package playwrigkt.skript.http

import playwrigkt.skript.Skript
import playwrigkt.skript.performer.SerializeCommand
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SerializeTroupe

data class HttpRequestDeserializationSkript<Body>(val bodyClass: Class<Body>): Skript<HttpRequest<ByteArray>, HttpRequest<Body>, SerializeTroupe> {
    override fun run(i: HttpRequest<ByteArray>, troupe: SerializeTroupe): AsyncResult<HttpRequest<Body>> =
            troupe
                    .getSerializePerformer()
                    .map { serializePerformer ->
                        i.flatMapBody { body -> serializePerformer.deserialize(SerializeCommand.Deserialize(body, bodyClass)) }
                    }
}
