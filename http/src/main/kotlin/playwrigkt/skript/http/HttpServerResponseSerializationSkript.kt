package playwrigkt.skript.http

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.performer.SerializeCommand
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult
import playwrigkt.skript.troupe.SerializeTroupe

data class HttpServerResponseSerializationSkript<I, Troupe>(
        val status: Skript<I, Http.Status, Troupe>,
        val headers: Skript<I, Map<String, List<String>>, Troupe>,
        val body: Skript<I, ByteArray, Troupe>,
        val error: Skript<Throwable, Http.Server.Response, Troupe>
): Skript<I, Http.Server.Response, Troupe> where Troupe: SerializeTroupe {
    override fun run(i: I, troupe: Troupe): AsyncResult<Http.Server.Response> {
        val statusResult = status.run(i, troupe)
        val headersResult = headers.run(i, troupe)
        val bodyResult = body.run(i, troupe)

        return statusResult
                .flatMap { status ->
                    headersResult.map { headers ->
                        Http.Server.Response(status, headers, bodyResult)
                    }
                }
                .recover { error.run(it, troupe) }
    }
}