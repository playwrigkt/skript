package playwrigkt.skript.http.client

import playwrigkt.skript.http.Http
import playwrigkt.skript.result.AsyncResult

sealed class HttpClient {
    data class Request(val method: Http.Method,
                       val uriTemplate: String,
                       val pathParameters: Map<String, String>,
                       val queryParameters: Map<String, String>,
                       val headers: Map<String, List<String>>,
                       val body: AsyncResult<ByteArray>): HttpClient() {
        fun uri(): String =
                "${uriWithPath()}?${queryParameters.map { "${it.key}=${it.value}" }.joinToString("&")}"


        private fun uriWithPath(): String =
                pathParameters.toList().fold(uriTemplate) { uri, parameter -> uri.replace("{${parameter.first}}", parameter.second) }
    }

    data class Response(
            val status: Http.Status,
            val headders: Map<String, List<String>>,
            val responseBody: AsyncResult<ByteArray>): HttpClient()
}