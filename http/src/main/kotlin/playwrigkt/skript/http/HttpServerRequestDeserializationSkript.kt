package playwrigkt.skript.http

import playwrigkt.skript.Skript
import playwrigkt.skript.performer.SerializeCommand
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SerializeTroupe

data class HttpServerRequestDeserializationSkript<Body>(val bodyClass: Class<Body>): Skript<Http.Server.Request<ByteArray>, Http.Server.Request<Body>, SerializeTroupe> {
    override fun run(i: Http.Server.Request<ByteArray>, troupe: SerializeTroupe): AsyncResult<Http.Server.Request<Body>> =
            troupe
                    .getSerializePerformer()
                    .map { serializePerformer ->
                        i.flatMapBody { body -> serializePerformer.deserialize(SerializeCommand.Deserialize(body, bodyClass)) }
                    }
}
