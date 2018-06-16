package playwrigkt.skript.performer

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readBytes
import io.ktor.content.ByteArrayContent
import io.ktor.http.HeadersBuilder
import io.ktor.http.ParametersBuilder
import io.ktor.http.URLProtocol
import io.ktor.util.toMap
import playwrigkt.skript.coroutine.ex.suspendMap
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
                .suspendMap {
                    requestBuilder.body = ByteArrayContent(it)
                    client.request<HttpResponse>(requestBuilder)
                }
                .map { response ->
                    playwrigkt.skript.http.client.HttpClient.Response(
                            Http.Status(response.status.value, response.status.description),
                            response.headers.toMap(),
                            AsyncResult.succeeded(response).suspendMap { it.readBytes() })
                }
    }

    fun HeadersBuilder.appendAll(headers: Map<String, List<String>>): HeadersBuilder {
        headers.forEach { k, v ->
            this.appendAll(k, v)
        }
        return this
    }

    fun ParametersBuilder.appendAll(parameters: Map<String, String>): ParametersBuilder {
        parameters.forEach { k, v ->
            this.append(k, v)
        }
        return this
    }

}