package playwrigkt.skript.http

data class HttpEndpoint(
        val path: String = "/",
        val headers: Map<String, List<String>>,
        val method: HttpMethod
)

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
    object Connect: HttpMethod()
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
        val body: ByteArray
)

data class HttpResponse(
        val status: Int,
        val responseBody: ByteArray
)