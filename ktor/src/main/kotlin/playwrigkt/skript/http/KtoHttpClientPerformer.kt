package playwrigkt.skript.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HeadersBuilder
import playwrigkt.skript.performer.HttpClientPerformer
import playwrigkt.skript.result.AsyncResult

class KtoHttpClientPerformer(val client: HttpClient): HttpClientPerformer {


    override fun perform(httpClientRequest: playwrigkt.skript.http.client.HttpClient.Request): AsyncResult<playwrigkt.skript.http.client.HttpClient.Response> {
        val requestBuilder = HttpRequestBuilder()
        requestBuilder.method = method(httpClientRequest.method)
        requestBuilder.headers { this.appendAll(httpClientRequest.headers) }
//        requestBuilder.url = URLBuilder.
//        client.call(
//                requestBuilder
//
//
//
//        )
        TODO("it")
    }
    fun HeadersBuilder.appendAll(headers: Map<String, List<String>>) {
        headers.forEach { k, v ->
            this.appendAll(k, v)
        }
    }

}