package playwrigkt.skript.performer

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import playwrigkt.skript.http.Http
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.vertx.ex.toMap

data class VertxHttpClientPerformer(val httpClient: HttpClient): HttpClientPerformer {
    override fun perform(httpClientRequest: playwrigkt.skript.http.client.HttpClient.Request): AsyncResult<playwrigkt.skript.http.client.HttpClient.Response> {
        val vertxResult = CompletableResult<HttpClientResponse>()
        method(httpClientRequest)
                .applyHeaders(httpClientRequest)
                .handler(vertxResult::succeed)
                .exceptionHandler(vertxResult::fail)
                .body(httpClientRequest)

        return vertxResult.map {
            val result = CompletableResult<Buffer>()
            it.bodyHandler(result::succeed)
            playwrigkt.skript.http.client.HttpClient.Response(
                    Http.Status(it.statusCode(), it.statusMessage()),
                    it.headers().toMap(),
                    result.map { it.bytes })
        }
    }

    fun method(httpClientRequest: playwrigkt.skript.http.client.HttpClient.Request):  HttpClientRequest =
        when(httpClientRequest.method) {
            Http.Method.Get -> httpClient.get(httpClientRequest.uri.materialized)
            Http.Method.Put -> httpClient.put(httpClientRequest.uri.materialized)
            Http.Method.Delete -> httpClient.delete(httpClientRequest.uri.materialized)
            Http.Method.Post -> httpClient.post(httpClientRequest.uri.materialized)
            Http.Method.Head -> httpClient.head(httpClientRequest.uri.materialized)
            Http.Method.Options -> httpClient.options(httpClientRequest.uri.materialized)
            Http.Method.Trace -> httpClient.request(HttpMethod.TRACE, httpClientRequest.uri.materialized)
            Http.Method.Connect -> httpClient.request(HttpMethod.CONNECT, httpClientRequest.uri.materialized)
            Http.Method.Patch -> httpClient.request(HttpMethod.PATCH, httpClientRequest.uri.materialized)
            is Http.Method.Other -> httpClient.request(HttpMethod.OTHER, httpClientRequest.uri.materialized)
            Http.Method.All -> httpClient.get(httpClientRequest.uri.materialized)
        }

    private fun HttpClientRequest.applyHeaders(clientRequest: playwrigkt.skript.http.client.HttpClient.Request): HttpClientRequest =
            clientRequest.headers.toList().fold(this) { vertxRequest, header -> vertxRequest.putHeader(header.first, header.second) }

    private fun HttpClientRequest.body(clientRequest: playwrigkt.skript.http.client.HttpClient.Request): AsyncResult<Unit> =
            clientRequest.body.map { body ->
                this
                        .putHeader("Content-Length", body.size.toString())
                        .write(io.vertx.core.buffer.Buffer.buffer(body))
                        .end()
            }
}