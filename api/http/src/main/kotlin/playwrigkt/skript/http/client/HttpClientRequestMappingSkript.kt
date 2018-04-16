package playwrigkt.skript.http.client

import playwrigkt.skript.Skript
import playwrigkt.skript.http.Http
import playwrigkt.skript.result.AsyncResult

data class HttpClientRequestMappingSkript<I, Troupe>(val method: Http.Method,
                                                     val uri: Skript<I, HttpClient.URI, Troupe>,
                                                     val headers: Skript<I, Map<String, List<String>>, Troupe> = Skript.map { emptyMap() },
                                                     val body: Skript<I, ByteArray, Troupe> = Skript.map { ByteArray(0) }): Skript<I, HttpClient.Request, Troupe> {
    override fun run(i: I, troupe: Troupe): AsyncResult<HttpClient.Request> {
        val uriResult = uri.run(i, troupe)
        val headersResult = headers.run(i, troupe)
        val bodyFuture = body.run(i, troupe)
        return uriResult.flatMap { uri ->
            headersResult.map { headers ->
                HttpClient.Request(method, uri, headers, bodyFuture)
            } }
    }
}