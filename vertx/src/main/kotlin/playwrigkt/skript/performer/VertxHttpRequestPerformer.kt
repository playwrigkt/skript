package playwrigkt.skript.performer

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import playwrigkt.skript.http.*
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult

data class VertxHttpRequestPerformer(val httpClient: HttpClient): HttpRequestPerformer {
    override fun perform(httpClientRequest: Http.Client.Request): AsyncResult<Http.Client.Response> {
        val vertxResult = CompletableResult<io.vertx.core.http.HttpClientResponse>()
        method(httpClientRequest)
                .applyHeaders(httpClientRequest)
                .handler(vertxResult::succeed)
                .exceptionHandler(vertxResult::fail)
                .body(httpClientRequest)

        return vertxResult.map {
            val result = CompletableResult<Buffer>()
            it.bodyHandler(result::succeed)
            Http.Client.Response(
                    it.statusCode(),
                    result.map { it.bytes })
        }
    }
    fun method(httpClientRequest: Http.Client.Request):  io.vertx.core.http.HttpClientRequest =
        when(httpClientRequest.method) {
            HttpMethod.Get -> httpClient.get(httpClientRequest.uri())
            HttpMethod.Put -> httpClient.put(httpClientRequest.uri())
            HttpMethod.Delete -> httpClient.delete(httpClientRequest.uri())
            HttpMethod.Post -> httpClient.post(httpClientRequest.uri())
            HttpMethod.Head -> httpClient.head(httpClientRequest.uri())
            HttpMethod.Options -> httpClient.options(httpClientRequest.uri())
            HttpMethod.Trace -> httpClient.request(io.vertx.core.http.HttpMethod.TRACE, httpClientRequest.uri())
            HttpMethod.Connect -> httpClient.request(io.vertx.core.http.HttpMethod.CONNECT, httpClientRequest.uri())
            HttpMethod.Patch -> httpClient.request(io.vertx.core.http.HttpMethod.PATCH, httpClientRequest.uri())
            is HttpMethod.Other -> httpClient.request(io.vertx.core.http.HttpMethod.OTHER, httpClientRequest.uri())
            HttpMethod.All -> httpClient.get(httpClientRequest.uri())
        }

    private fun io.vertx.core.http.HttpClientRequest.applyHeaders(clientRequest: Http.Client.Request): io.vertx.core.http.HttpClientRequest =
            clientRequest.headers.toList().fold(this) { vertxRequest, header -> vertxRequest.putHeader(header.first, header.second) }

    private fun io.vertx.core.http.HttpClientRequest.body(clientRequest: Http.Client.Request): AsyncResult<Unit> =
            clientRequest.body.map { body ->
                this
                        .putHeader("Content-Length", body.size.toString())
                        .write(io.vertx.core.buffer.Buffer.buffer(body))
                        .end()
            }
}