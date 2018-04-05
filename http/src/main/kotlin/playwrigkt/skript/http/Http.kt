package playwrigkt.skript.http

import playwrigkt.skript.result.AsyncResult

sealed class HttpError: Throwable() {
    data class EndpointAlreadyMatches(val existing: HttpEndpoint, val duplicate: HttpEndpoint): HttpError()
    data class EndpointNotHandled(val endpoint: HttpEndpoint): HttpError()
    object AlreadyStopped: HttpError()
}
data class HttpEndpoint(
        val path: String = "/",
        val headers: Map<String, List<String>>,
        val method: HttpMethod
) {
    fun matches(request: HttpRequest): Boolean =
        methodMatches(request.method) &&
                pathMathes(request.path) &&
                headersMatch(request.headers)

    fun matches(endpoint: HttpEndpoint): Boolean =
            methodMatches(endpoint.method) &&
                    pathMathes(endpoint.path) &&
                    headersMatch(endpoint.headers)

    private fun pathMathes(path: String): Boolean =
            this.path.removePrefix("/").equals(path.removePrefix("/"))

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

data class HttpRequest(
        val requestUri: String,
        val method: HttpMethod,
        val headers: Map<String, List<String>>,
        val body: AsyncResult<ByteArray>
) {
    val path by lazy {
        requestUri.substringBefore("?")
    }

    val params by lazy {
        requestUri.substringAfter("?")
                .split("&")
                .map { it.split("=") }
                .filter { it.size == 2 }
                .map { it.get(0) to it.get(1) }
    }
}

data class HttpResponse(
        val status: Int,
        val responseBody: ByteArray
)