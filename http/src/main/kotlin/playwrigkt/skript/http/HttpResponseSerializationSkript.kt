package playwrigkt.skript.http

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.performer.SerializeCommand
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult
import playwrigkt.skript.troupe.SerializeTroupe

data class HttpResponseSerializationSkript<I, Troupe>(
        val errorMappig: (Throwable) -> Http.Server.Response
): Skript<I, Http.Server.Response, Troupe> where Troupe: SerializeTroupe {
    override fun run(i: I, troupe: Troupe): AsyncResult<Http.Server.Response> =
            troupe.getSerializePerformer()
                    .flatMap { serializePerformer -> serializePerformer.serialize(SerializeCommand.Serialize(i)) }
                    .map { Http.Server.Response(200, "OK", emptyMap(), it) }
                    .recover { Try { errorMappig(it) }.toAsyncResult() }
}