package playwrigkt.skript.performer

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import playwrigkt.skript.vertx.ex.toMap
import playwrigkt.skript.http.*
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult

data class VertxHttpClientPerformer(val httpClient: HttpClient): HttpClientPerformer {
    override fun perform(httpClientRequest: Http.Client.Request): AsyncResult<Http.Client.Response> {
        val vertxResult = CompletableResult<HttpClientResponse>()
        method(httpClientRequest)
                .applyHeaders(httpClientRequest)
                .handler(vertxResult::succeed)
                .exceptionHandler(vertxResult::fail)
                .body(httpClientRequest)

        return vertxResult.map {
            val result = CompletableResult<Buffer>()
            it.bodyHandler(result::succeed)
            Http.Client.Response(
                    Http.Status(it.statusCode(), it.statusMessage()),
                    it.headers().toMap(),
                    result.map { it.bytes })
        }
    }

    fun method(httpClientRequest: Http.Client.Request):  HttpClientRequest =
        when(httpClientRequest.method) {
            Http.Method.Get -> httpClient.get(httpClientRequest.uri())
            Http.Method.Put -> httpClient.put(httpClientRequest.uri())
            Http.Method.Delete -> httpClient.delete(httpClientRequest.uri())
            Http.Method.Post -> httpClient.post(httpClientRequest.uri())
            Http.Method.Head -> httpClient.head(httpClientRequest.uri())
            Http.Method.Options -> httpClient.options(httpClientRequest.uri())
            Http.Method.Trace -> httpClient.request(HttpMethod.TRACE, httpClientRequest.uri())
            Http.Method.Connect -> httpClient.request(HttpMethod.CONNECT, httpClientRequest.uri())
            Http.Method.Patch -> httpClient.request(HttpMethod.PATCH, httpClientRequest.uri())
            is Http.Method.Other -> httpClient.request(HttpMethod.OTHER, httpClientRequest.uri())
            Http.Method.All -> httpClient.get(httpClientRequest.uri())
        }

    private fun HttpClientRequest.applyHeaders(clientRequest: Http.Client.Request): HttpClientRequest =
            clientRequest.headers.toList().fold(this) { vertxRequest, header -> vertxRequest.putHeader(header.first, header.second) }

    private fun HttpClientRequest.body(clientRequest: Http.Client.Request): AsyncResult<Unit> =
            clientRequest.body.map { body ->
                this
                        .putHeader("Content-Length", body.size.toString())
                        .write(io.vertx.core.buffer.Buffer.buffer(body))
                        .end()
            }
}