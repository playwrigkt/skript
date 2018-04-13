package playwrigkt.skript.venue

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import org.funktionale.option.firstOption
import org.funktionale.tries.Try
import org.slf4j.LoggerFactory
import playwrigkt.skript.Skript
import playwrigkt.skript.vertx.ex.toMap
import playwrigkt.skript.http.*
import playwrigkt.skript.produktion.VertxHttpProduktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.toAsyncResult
import playwrigkt.skript.stagemanager.StageManager

class VertxHttpVenue(val server: HttpServer): HttpServerVenue {
    val log = LoggerFactory.getLogger(this::class.java)

    private val requestHandlers: MutableList<VertxHttpProduktion<*>> = mutableListOf()

    init {
        server.requestHandler { serverRequest ->
            val method = serverRequest.method().toHttpMethod()
            val headers = serverRequest.headers().toMap()
            val uri = serverRequest.absoluteURI()
            val body = serverRequest.body()
            val path = serverRequest.path()

            log.debug("handling request \n\tmethod: {}\n\theaders: {}\n\turi: {}\n\tbody: {}\n\t: path", method, headers, uri, body, path)

            requestHandlers
                    .firstOption { it.endpoint.matches(method, headers, path)}
                    .orNull()
                    ?.let {produktion -> produktion.endpoint.request(uri, method, headers, body, path).flatMap(produktion::invoke) }
                    ?.map { serverRequest.response()
                            .setStatusCode(it.status.code)
                            .setStatusMessage(it.status.message)
                            .putHeaders(it.headers)
                            .end(Buffer.buffer(it.responseBody))
                    }
                    ?.recover {
                        log.debug("error handling request: {}", it)
                        Try { serverRequest.response().setStatusCode(500).end() }.toAsyncResult()
                    }
                    ?: serverRequest.response().setStatusCode(501).end("Not Implemented")
        }
        server.listen()
    }


    private fun HttpServerResponse.putHeaders(headers: Map<String, List<String>>): HttpServerResponse {
        headers.forEach { k, v ->
            this.putHeader(k, v)
        }
        return this
    }
    private fun HttpMethod.toHttpMethod(): Http.Method =
            when(this) {
                HttpMethod.POST -> Http.Method.Post
                HttpMethod.GET -> Http.Method.Get
                HttpMethod.PUT -> Http.Method.Put
                HttpMethod.PATCH-> Http.Method.Patch
                HttpMethod.DELETE -> Http.Method.Delete
                HttpMethod.HEAD -> Http.Method.Head
                HttpMethod.OPTIONS -> Http.Method.Options
                HttpMethod.TRACE -> Http.Method.Trace
                HttpMethod.CONNECT -> Http.Method.Connect
                HttpMethod.OTHER -> Http.Method.Other("")
            }


    private fun HttpServerRequest.body(): AsyncResult<ByteArray> {
        val result = CompletableResult<Buffer>()
        this.bodyHandler(result::succeed)
        return result.map { it.bytes }
    }

    override fun <Troupe> produktion(skript: Skript<Http.Server.Request<ByteArray>, Http.Server.Response, Troupe>,
                                     stageManager: StageManager<Troupe>,
                                     rule: HttpEndpoint): AsyncResult<VertxHttpProduktion<Troupe>> =
        Try {
            requestHandlers
                    .find { it.endpoint.matches(rule) }
                    ?.let { throw HttpError.EndpointAlreadyMatches(it.endpoint, rule) }
                    ?:VertxHttpProduktion(rule, this, skript, stageManager)
        }
                .onSuccess { requestHandlers.add(it) }
                .toAsyncResult()

    fun removeHandler(httpEndpoint: HttpEndpoint): Try<Unit> =
        Try {
            if(!requestHandlers.removeAll { it.endpoint.matches(httpEndpoint) }) {
                throw HttpError.EndpointNotHandled(httpEndpoint)
            }
        }

    fun handles(httpEndpoint: HttpEndpoint): Boolean =
        requestHandlers.any { it.endpoint.matches(httpEndpoint) }
}