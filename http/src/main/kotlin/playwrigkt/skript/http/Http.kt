package playwrigkt.skript.http

import playwrigkt.skript.result.AsyncResult
import org.funktionale.option.getOrElse
import org.funktionale.option.toOption
import org.funktionale.tries.Try
import playwrigkt.skript.result.toAsyncResult

sealed class HttpError: Throwable() {
    data class EndpointAlreadyMatches(val existing: HttpEndpoint, val duplicate: HttpEndpoint): HttpError()
    data class EndpointNotHandled(val endpoint: HttpEndpoint): HttpError()
    data class PathUnparsable(val path: String, val endpoint: HttpEndpoint): HttpError()
    data class MissingInputs(val inputs: List<HttpInput>): HttpError()
    object AlreadyStopped: HttpError()

    sealed class Client: HttpError() {
        data class UnhandledResponse(val response: Http.Client.Response) : HttpError.Client()
    }

    data class HttpInput(val inputType: String, val name: String) {
        companion object {
            fun header(name: String): HttpInput = HttpInput("header", name)
            fun path(name: String): HttpInput = HttpInput("path", name)
            fun query(name: String): HttpInput = HttpInput("query", name)
        }
    }
}


data class HttpEndpoint(
        val path: String = "/",
        val headers: Map<String, List<String>>,
        val method: Http.Method) {
    val pathRule by lazy {
        HttpPathRule(path.removePrefix("/"))
    }
    companion object {
        fun queryParams(uri: String): Map<String, String> =
                uri.substringAfter("?").toOption()
                        .filter { it.isNotBlank() }
                        .map { it.split("&")
                                .map { it.split("=") }
                                .filter { it.size == 2 }
                                .map { it.get(0) to it.get(1) }
                                .toMap()
                        }
                        .getOrElse { emptyMap() }

    }

    fun matches(requestMethod: Http.Method, requestHeaders: Map<String, List<String>>, path: String): Boolean =
            this.methodMatches(requestMethod) &&
                    this.headersMatch(requestHeaders) &&
                    this.pathMatches(path)

    fun matches(httpEndpoint: HttpEndpoint): Boolean =
            this.methodMatches(httpEndpoint.method) &&
                    this.headersMatch(httpEndpoint.headers) &&
                    this.pathMatches(httpEndpoint.path)

    fun <T> request(requestUri: String,
               method: Http.Method,
               headers: Map<String, List<String>>,
               body: AsyncResult<T>,
               path: String): AsyncResult<Http.Server.Request<T>> =
            pathRule.apply(path.removePrefix("/").removeSuffix("/"))
                    .map {  Try { Http.Server.Request<T>(method, requestUri, it, queryParams(requestUri), headers, body) } }
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

sealed class Http {
    sealed class Method {
        fun matches(other: Method) =
                when(other) {
                    is All -> true
                    else -> this.equals(other)
                }

        object Get: Method()
        object Put: Method();
        object Delete: Method();
        object Post: Method()
        object Head: Method()
        object Options: Method()
        object Trace: Method()
        object Connect: Method()
        object Patch: Method()
        data class Other(val name: String): Method()
        object All: Method() {
            override fun equals(other: Any?): Boolean =
                    when(other) {
                        is Method -> true
                        else -> false
                    }
        }
    }
    sealed class Server: Http() {
        data class Request<T>(val method: Method,
                              val requestUri: String,
                              val pathParameters: Map<String, String>,
                              val queryParameters: Map<String, String>,
                              val headers: Map<String, List<String>>,
                              val body: AsyncResult<T>): Server() {

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
                val status: Int,
                val responseBody: ByteArray
        ): Server()
    }
    sealed class Client: Http() {
        data class Request(val method: Method,
                           val uriTemplate: String,
                           val pathParameters: Map<String, String>,
                           val queryParameters: Map<String, String>,
                           val headers: Map<String, List<String>>,
                           val body: AsyncResult<ByteArray>): Client() {
            fun uri(): String =
                    "${uriWithPath()}?${queryParameters.map { "${it.key}=${it.value}" }.joinToString("&")}"


            private fun uriWithPath(): String =
                    pathParameters.toList().fold(uriTemplate) { uri, parameter -> uri.replace("{${parameter.first}}", parameter.second) }
        }

        data class Response(
                val status: Int,
                val responseBody: AsyncResult<ByteArray>): Client()
    }

}
