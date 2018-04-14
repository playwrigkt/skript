package playwrigkt.skript.http.client

import playwrigkt.skript.http.Http
import playwrigkt.skript.result.AsyncResult

sealed class HttpClient {
    data class URI(val ssl: Boolean,
                   val host: String,
                   val port: Int?,
                   val pathTemplate: String,
                   val pathParameters: Map<String, String>,
                   val queryParameters: Map<String, String>) {
        val materialized: String by lazy {
            "${uriBase}/${pathString.removePrefix("/")}?${queryString}"
        }

        private val uriBase: String = "http${if(ssl) "s" else "" }://$host${port?.let{":$it"}}"

        private val pathString: String by lazy {
            pathParameters
                    .toList()
                    .fold(pathTemplate) { uri, parameter ->
                        uri.replace("{${parameter.first}}", parameter.second)
                    }
        }

        val queryString: String by lazy {
            queryParameters
                    .map { "${it.key}=${it.value}" }
                    .joinToString("&")
        }
    }

    data class Request(val method: Http.Method,
                       val uri: URI,
                       val headers: Map<String, List<String>>,
                       val body: AsyncResult<ByteArray>): HttpClient() {

    }

    data class Response(
            val status: Http.Status,
            val headders: Map<String, List<String>>,
            val responseBody: AsyncResult<ByteArray>): HttpClient()
}