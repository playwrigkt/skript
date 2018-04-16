package playwrigkt.skript.http.server

import org.funktionale.option.getOrElse
import org.funktionale.option.toOption
import org.funktionale.tries.Try
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.HttpError
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult

sealed class HttpServer {
    data class Endpoint(
            val path: String = "/",
            val headers: Map<String, List<String>>,
            val method: Http.Method): HttpServer() {
        val pathRule by lazy {
            HttpPathRule(path.removePrefix("/"))
        }
        companion object {
            fun queryParams(query: String): Map<String, List<String>> =
                    query.removePrefix("?").toOption()
                            .filter { it.isNotBlank() }
                            .map { it.split("&")
                                    .map { it.split("=") }
                                    .filter { it.size == 2 }
                                    .map { it.get(0) to it.get(1) }
                                    .groupBy { it.first }
                                    .map { it.key to it.value.map { it.second } }
                                    .toMap()
                            }
                            .getOrElse { emptyMap() }

        }

        fun matches(requestMethod: Http.Method, requestHeaders: Map<String, List<String>>, path: String): Boolean =
                this.methodMatches(requestMethod) &&
                        this.headersMatch(requestHeaders) &&
                        this.pathMatches(path)

        fun matches(httpEndpoint: Endpoint): Boolean =
                this.methodMatches(httpEndpoint.method) &&
                        this.headersMatch(httpEndpoint.headers) &&
                        this.pathMatches(httpEndpoint.path)

        fun <T> request(requestUri: String,
                        method: Http.Method,
                        path: String,
                        query: String,
                        headers: Map<String, List<String>>,
                        body: AsyncResult<T>): AsyncResult<Request<T>> =
                pathRule.apply(path.removePrefix("/").removeSuffix("/"))
                        .map { Try { Request<T>(method, requestUri, it, queryParams(query), headers, body) } }
                        .getOrElse { Try.Failure(HttpError.PathUnparsable(path, this)) }
                        .toAsyncResult()

        private fun pathMatches(path: String): Boolean =
                this.pathRule.pathMatches(path.removePrefix("/"))

        private fun headersMatch(headers: Map<String, List<String>>): Boolean =
                this.headers.all {
                    headers.get(it.key)?.containsAll(it.value)?:false
                }

        private fun methodMatches(method: Http.Method): Boolean =
                this.method.matches(method)
    }

    data class Request<T>(val method: Http.Method,
                          val requestUri: String,
                          val pathParameters: Map<String, String>,
                          val queryParameters: Map<String, List<String>>,
                          val headers: Map<String, List<String>>,
                          val body: AsyncResult<T>): HttpServer() {
        fun <U> flatMapBody(parse: (T) -> AsyncResult<U>): Request<U> =
                Request(
                        method,
                        requestUri,
                        pathParameters,
                        queryParameters,
                        headers,
                        body.flatMap(parse))

        fun <U> mapBody(parse: (T) -> U): Request<U> =
                Request(
                        method,
                        requestUri,
                        pathParameters,
                        queryParameters,
                        headers,
                        body.map(parse))
    }
    data class Response(
            val status: Http.Status,
            val headers: Map<String, List<String>>,
            val responseBody: AsyncResult<ByteArray>
    ): HttpServer()
}