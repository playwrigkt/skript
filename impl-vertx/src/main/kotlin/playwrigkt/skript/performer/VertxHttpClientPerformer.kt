package playwrigkt.skript.performer

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.*
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

    fun playwrigkt.skript.http.client.HttpClient.URI.toRequestOptions(): RequestOptions =
            RequestOptions()
                    .setHost(this.host)
                    .let { this.port?.let(it::setPort)?:it }
                    .setURI(this.uri)
                    .setSsl(this.ssl)

    fun method(httpClientRequest: playwrigkt.skript.http.client.HttpClient.Request):  HttpClientRequest =
        when(httpClientRequest.method) {
            Http.Method.Get -> httpClient.get(httpClientRequest.uri.toRequestOptions())
            Http.Method.Put -> httpClient.put(httpClientRequest.uri.toRequestOptions())
            Http.Method.Delete -> httpClient.delete(httpClientRequest.uri.toRequestOptions())
            Http.Method.Post -> httpClient.post(httpClientRequest.uri.toRequestOptions())
            Http.Method.Head -> httpClient.head(httpClientRequest.uri.toRequestOptions())
            Http.Method.Options -> httpClient.options(httpClientRequest.uri.toRequestOptions())
            Http.Method.Trace -> httpClient.request(HttpMethod.TRACE, httpClientRequest.uri.toRequestOptions())
            Http.Method.Connect -> httpClient.request(HttpMethod.CONNECT, httpClientRequest.uri.toRequestOptions())
            Http.Method.Patch -> httpClient.request(HttpMethod.PATCH, httpClientRequest.uri.toRequestOptions())
            is Http.Method.Other -> httpClient.request(HttpMethod.OTHER, httpClientRequest.uri.toRequestOptions())
            Http.Method.All -> httpClient.get(httpClientRequest.uri.toRequestOptions())
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