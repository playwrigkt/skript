package playwrigkt.skript.http

import playwrigkt.skript.result.AsyncResult
import org.funktionale.option.Option
import org.funktionale.option.getOrElse
import org.funktionale.tries.Try
import playwrigkt.skript.result.toAsyncResult

sealed class HttpError: Throwable() {
    data class EndpointAlreadyMatches(val existing: HttpEndpoint, val duplicate: HttpEndpoint): HttpError()
    data class EndpointNotHandled(val endpoint: HttpEndpoint): HttpError()
    data class PathUnparsable(val path: String, val endpoint: HttpEndpoint): HttpError()
    data class MissingInputs(val inputs: List<HttpInput>): HttpError()
    object AlreadyStopped: HttpError()


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
        val method: HttpMethod) {
    val pathRule by lazy {
        HttpPathRule(path.removePrefix("/"))
    }

    fun matches(requestMethod: HttpMethod, requestHeaders: Map<String, List<String>>, path: String): Boolean =
            this.methodMatches(requestMethod) &&
                    this.headersMatch(requestHeaders) &&
                    this.pathMatches(path)

    fun matches(httpEndpoint: HttpEndpoint): Boolean =
            this.methodMatches(httpEndpoint.method) &&
                    this.headersMatch(httpEndpoint.headers) &&
                    this.pathMatches(httpEndpoint.path)

    fun <T> request(requestUri: String,
               method: HttpMethod,
               headers: Map<String, List<String>>,
               body: AsyncResult<T>,
               path: String): AsyncResult<HttpServerRequest<T>> =
            pathRule.apply(path.removePrefix("/").removeSuffix("/"))
                    .map {  Try { HttpServerRequest<T>(requestUri, method, headers, body, it) } }
                    .getOrElse { Try.Failure(HttpError.PathUnparsable(path, this)) }
                    .toAsyncResult()

    private fun pathMatches(path: String): Boolean =
            this.pathRule.pathMatches(path.removePrefix("/"))

    private fun headersMatch(headers: Map<String, List<String>>): Boolean =
            this.headers.all {
                headers.get(it.key)?.containsAll(it.value)?:false
            }

    private fun methodMatches(method: HttpMethod): Boolean =
            this.method.matches(method)
}



sealed class HttpMethod {
    fun matches(other: HttpMethod) =
            when(other) {
                is All -> true
                else -> this.equals(other)
            }

    object Get: HttpMethod()
    object Put: HttpMethod();
    object Delete: HttpMethod();
    object Post: HttpMethod()
    object Head: HttpMethod()
    object Options: HttpMethod()
    object Trace: HttpMethod()
    object Connect: HttpMethod()
    object Patch: HttpMethod()
    data class Other(val name: String): HttpMethod()
    object All: HttpMethod() {
        override fun equals(other: Any?): Boolean =
                when(other) {
                    is HttpMethod -> true
                    else -> false
                }
    }
}

data class HttpServerRequest<T>(val requestUri: String,
                                val method: HttpMethod,
                                val headers: Map<String, List<String>>,
                                val body: AsyncResult<T>,
                                val pathParameters: Map<String, String>) {
    val queryParams by lazy {
        requestUri.substringAfter("?")
                .split("&")
                .map { it.split("=") }
                .filter { it.size == 2 }
                .map { it.get(0) to it.get(1) }
                .toMap()
    }

    fun <U> flatMapBody(parse: (T) -> AsyncResult<U>): HttpServerRequest<U> =
        HttpServerRequest(
                requestUri,
                method,
                headers,
                body.flatMap(parse),
                pathParameters)

    fun <U> mapBody(parse: (T) -> U): HttpServerRequest<U> =
            HttpServerRequest(
                    requestUri,
                    method,
                    headers,
                    body.map(parse),
                    pathParameters)
}

data class HttpServerResponse(
        val status: Int,
        val responseBody: ByteArray
)

data class HttpClientRequest(val uriTemplate: String,
                             val pathParameters: Map<String, String>,
                             val queryParameters: Map<String, String>,
                             val headers: Map<String, List<String>>,
                             val method: HttpMethod,
                             val body: AsyncResult<ByteArray>) {
    fun uri(): String =
            "${uriWithPath()}?${queryParameters.map { "${it.key}=${it.value}" }.joinToString("&")}"


    private fun uriWithPath(): String =
            pathParameters.toList().fold(uriTemplate) { uri, parameter -> uri.replace("{${parameter.first}}", parameter.second) }
}

data class HttpClientResponse(
        val status: Int,
        val responseBody: AsyncResult<ByteArray>
)