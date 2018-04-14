package playwrigkt.skript.http.client

import playwrigkt.skript.Skript
import playwrigkt.skript.http.Http
import playwrigkt.skript.result.AsyncResult

data class HttpClientRequestMappingSkript<I, Troupe>(val method: Http.Method,
                                                     val uri: Skript<I, HttpClient.URI, Troupe>,
                                                     val pathParameters: Skript<I, Map<String, String>, Troupe> = Skript.map { emptyMap() },
                                                     val queryParameters: Skript<I, Map<String, String>, Troupe> = Skript.map { emptyMap() },
                                                     val headers: Skript<I, Map<String, List<String>>, Troupe> = Skript.map { emptyMap() },
                                                     val body: Skript<I, ByteArray, Troupe> = Skript.map { ByteArray(0) }): Skript<I, HttpClient.Request, Troupe> {
    override fun run(i: I, troupe: Troupe): AsyncResult<HttpClient.Request> {
        val uriResult = uri.run(i, troupe)
        val headersResult = headers.run(i, troupe)
        val pathParametersResult = pathParameters.run(i, troupe)
        val queryParametersResult = queryParameters.run(i, troupe)
        val bodyFuture = body.run(i, troupe)
        return uriResult.flatMap { uri ->
            headersResult.flatMap { headers ->
            pathParametersResult.flatMap { pathParameters ->
            queryParametersResult.map { queryParameters ->
                HttpClient.Request(method, uri, headers, bodyFuture)
            } } } }
    }
}