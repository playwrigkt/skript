package playwrigkt.skript.http

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.performer.SerializeCommand
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult
import playwrigkt.skript.troupe.SerializeTroupe

data class HttpResponseSerializationSkript<I, Troupe>(
        val errorMappig: (Throwable) -> HttpServerResponse
): Skript<I, HttpServerResponse, Troupe> where Troupe: SerializeTroupe {
    override fun run(i: I, troupe: Troupe): AsyncResult<HttpServerResponse> =
            troupe.getSerializePerformer()
                    .flatMap { serializePerformer -> serializePerformer.serialize(SerializeCommand.Serialize(i)) }
                    .map { HttpServerResponse(200, it) }
                    .recover { Try { errorMappig(it) }.toAsyncResult() }
}