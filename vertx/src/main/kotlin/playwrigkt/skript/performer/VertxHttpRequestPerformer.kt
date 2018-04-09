package playwrigkt.skript.performer

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import playwrigkt.skript.http.*
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult

data class VertxHttpRequestPerformer(val httpClient: HttpClient): HttpRequestPerformer {
    override fun perform(httpClientRequest: HttpClientRequest): AsyncResult<HttpClientResponse> =
        when(httpClientRequest.method) {
            HttpMethod.Get ->
                httpClient
                        .get(httpClientRequest.uri())
                        .applyHeaders(httpClientRequest)
                        .handle()

            else -> TODO("other methods")
        }

    private fun io.vertx.core.http.HttpClientRequest.applyHeaders(clientRequest: HttpClientRequest): io.vertx.core.http.HttpClientRequest =
            clientRequest.headers.toList().fold(this) { vertxRequest, header -> vertxRequest.putHeader(header.first, header.second) }

    private fun io.vertx.core.http.HttpClientRequest.handle(): AsyncResult<HttpClientResponse> {
        val vertxResult = CompletableResult<io.vertx.core.http.HttpClientResponse>()

        this.handler(vertxResult::succeed)
                .exceptionHandler(vertxResult::fail)
                .end()

        return vertxResult.map {
            val result = CompletableResult<Buffer>()
            it.bodyHandler(result::succeed)
            HttpClientResponse(
                    it.statusCode(),
                    result.map { it.bytes })
        }
    }



}