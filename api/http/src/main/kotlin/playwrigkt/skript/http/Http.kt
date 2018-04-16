package playwrigkt.skript.http

import playwrigkt.skript.http.client.HttpClient
import playwrigkt.skript.http.server.HttpServer

sealed class HttpError: Throwable() {
    data class EndpointAlreadyMatches(val existing: HttpServer.Endpoint, val duplicate: HttpServer.Endpoint): HttpError()
    data class EndpointNotHandled(val endpoint: HttpServer.Endpoint): HttpError()
    data class PathUnparsable(val path: String, val endpoint: HttpServer.Endpoint): HttpError()
    data class MissingInputs(val inputs: List<HttpInput>): HttpError()
    object AlreadyStopped: HttpError()

    sealed class Client: HttpError() {
        data class UnhandledResponse(val response: HttpClient.Response) : HttpError.Client()
    }

    data class HttpInput(val inputType: String, val name: String) {
        companion object {
            fun header(name: String): HttpInput = HttpInput("header", name)
            fun path(name: String): HttpInput = HttpInput("path", name)
            fun query(name: String): HttpInput = HttpInput("query", name)
        }
    }
}

sealed class Http {
    data class Status(val code: Int, val message: String) {
        companion object {

            val OK = Status(200, "OK")
            val Created = Status(201, "Created")
            val Accepted = Status(202, "Accepted")
            val NonAuthoritativeInformation = Status(203, "Non-Authoritative Information")
            val NoContent = Status(204, "No Content")

            val BadRequest = Status(400, "Bad Request")
            val Unauthorized = Status(401, "Unauthorized")
            val PaymentRequest = Status(402, "Payment Required")
            val Forbidden = Status(403, "Forbidden")
            val NotFound = Status(404, "Not Found")
            val MethodNotAlowed = Status(405, "Method Not Allowed")
            val ImATeapot = Status(418, "I'm a teapot")

            val InternalServerError = Status(500, "Internal Server Error")
            val NotImplemented = Status(501, "Not Implemented")
            val BadGateway = Status(502, "Bad Gateway")
            val ServiceUnavailable = Status(503, "Service Unavailable")
            val GatewayTimeout = Status(504, "GatewayTimeout")

            //Todo implement status messages
        }
    }
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

}
