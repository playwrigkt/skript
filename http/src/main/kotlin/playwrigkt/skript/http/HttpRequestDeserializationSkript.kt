package playwrigkt.skript.http

import playwrigkt.skript.Skript
import playwrigkt.skript.performer.SerializeCommand
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SerializeTroupe

data class HttpRequestDeserializationSkript<Body>(val bodyClass: Class<Body>): Skript<HttpServerRequest<ByteArray>, HttpServerRequest<Body>, SerializeTroupe> {
    override fun run(i: HttpServerRequest<ByteArray>, troupe: SerializeTroupe): AsyncResult<HttpServerRequest<Body>> =
            troupe
                    .getSerializePerformer()
                    .map { serializePerformer ->
                        i.flatMapBody { body -> serializePerformer.deserialize(SerializeCommand.Deserialize(body, bodyClass)) }
                    }
}
