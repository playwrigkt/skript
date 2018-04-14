package playwrigkt.skript.performer

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.response.readBytes
import io.ktor.content.ByteArrayContent
import io.ktor.http.HeadersBuilder
import io.ktor.http.ParametersBuilder
import io.ktor.http.URLProtocol
import io.ktor.util.toMap
import playwrigkt.skript.coroutine.ex.mapSuspend
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.method
import playwrigkt.skript.result.AsyncResult

class KtorHttpClientPerformer(val client: HttpClient): HttpClientPerformer {


    override fun perform(httpClientRequest: playwrigkt.skript.http.client.HttpClient.Request): AsyncResult<playwrigkt.skript.http.client.HttpClient.Response> {
        val requestBuilder = HttpRequestBuilder()
        requestBuilder.method = method(httpClientRequest.method)
        requestBuilder.headers { this.appendAll(httpClientRequest.headers) }
        requestBuilder.url {
            this.protocol = if(httpClientRequest.uri.ssl) URLProtocol.HTTPS else URLProtocol.HTTP
            this.host = httpClientRequest.uri.host
            httpClientRequest.uri.port?.let { this.port = it }
            this.path(httpClientRequest.uri.pathParts)
            this.parameters.appendAll(httpClientRequest.uri.pathParameters)
        }

        return httpClientRequest.body
                .map { requestBuilder.body = ByteArrayContent(it) }
                .mapSuspend { client.call(requestBuilder) }
                .map {
                    playwrigkt.skript.http.client.HttpClient.Response(
                            Http.Status(it.response.status.value, it.response.status.description),
                            it.response.headers.toMap(),
                            AsyncResult.succeeded(it.response).mapSuspend { it.readBytes() })
                }
    }
    fun HeadersBuilder.appendAll(headers: Map<String, List<String>>) {
        headers.forEach { k, v ->
            this.appendAll(k, v)
        }
    }
    fun ParametersBuilder.appendAll(parameters: Map<String, String>) {
        parameters.forEach { k, v ->
            this.append(k, v)
        }
    }

}