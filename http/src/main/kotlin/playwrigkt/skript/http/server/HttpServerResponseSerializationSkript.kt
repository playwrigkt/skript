package playwrigkt.skript.http.server

import playwrigkt.skript.Skript
import playwrigkt.skript.http.Http
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SerializeTroupe

data class HttpServerResponseSerializationSkript<I, Troupe>(
        val status: Skript<I, Http.Status, Troupe>,
        val headers: Skript<I, Map<String, List<String>>, Troupe>,
        val body: Skript<I, ByteArray, Troupe>,
        val error: Skript<Throwable, HttpServer.Response, Troupe>
): Skript<I, HttpServer.Response, Troupe> where Troupe: SerializeTroupe {
    override fun run(i: I, troupe: Troupe): AsyncResult<HttpServer.Response> {
        val statusResult = status.run(i, troupe)
        val headersResult = headers.run(i, troupe)
        val bodyResult = body.run(i, troupe)

        return statusResult
                .flatMap { status ->
                    headersResult.map { headers ->
                        HttpServer.Response(status, headers, bodyResult)
                    }
                }
                .recover { error.run(it, troupe) }
    }
}