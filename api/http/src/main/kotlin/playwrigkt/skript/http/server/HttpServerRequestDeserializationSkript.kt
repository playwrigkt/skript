package playwrigkt.skript.http.server

import playwrigkt.skript.Skript
import playwrigkt.skript.performer.SerializeCommand
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SerializeTroupe

data class HttpServerRequestDeserializationSkript<Body>(val bodyClass: Class<Body>): Skript<HttpServer.Request<ByteArray>, HttpServer.Request<Body>, SerializeTroupe> {
    override fun run(i: HttpServer.Request<ByteArray>, troupe: SerializeTroupe): AsyncResult<HttpServer.Request<Body>> =
            troupe
                    .getSerializePerformer()
                    .map { serializePerformer ->
                        i.flatMapBody { body -> serializePerformer.deserialize(SerializeCommand.Deserialize(body, bodyClass)) }
                    }
}
