package playwrigkt.skript.venue

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import arrow.core.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.HttpError
import playwrigkt.skript.produktion.VertxHttpProduktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.vertx.ex.toMap
import playwrigkt.skript.vertx.ex.vertxHandler

data class VertxHttpServerVenue(val vertx: Vertx, val httpServerOptions: HttpServerOptions): HttpServerVenue() {
    private val server by lazy {
        vertx.createHttpServer(httpServerOptions)
    }

    private val requestHandlers: MutableList<VertxHttpProduktion<*>> = mutableListOf()

    init {
        server.requestHandler { serverRequest ->
            val method = serverRequest.method().toHttpMethod()
            val headers = serverRequest.headers().toMap()
            val uri = serverRequest.absoluteURI()
            val body = serverRequest.body()
            val path = serverRequest.path()
            val query = serverRequest.query()
            log.debug("handling request \n\tmethod: {}\n\theaders: {}\n\turi: {}\n\tbody: {}\n\t: path", method, headers, uri, body, path)

            requestHandlers
                    .firstOrNull { it.endpoint.matches(method, headers, path)}
                    ?.let {produktion -> produktion.endpoint.request(uri, method, path, query, headers, body).flatMap(produktion::invoke) }
                    ?.flatMap {
                        val result = CompletableResult<Unit>()
                        val request = serverRequest.response()
                                .bodyEndHandler { result.succeed(Unit) }
                                .exceptionHandler(result::fail)
                                .setStatusCode(it.status.code)
                                .setStatusMessage(it.status.message)
                                .putHeaders(it.headers)

                                it.responseBody.map {
                                    request.end(Buffer.buffer(it))
                                }

                        result
                    }
                    ?.recover {
                        log.debug("error handling request: {}", it)
                        Try { serverRequest.response().setStatusCode(500).end() }.toAsyncResult()
                    }
                    ?: serverRequest.response().setStatusCode(501).end("Not Implemented")
        }
        server.listen()
    }

    override fun <Troupe> createProduktion(skript: Skript<playwrigkt.skript.http.server.HttpServer.Request<ByteArray>, playwrigkt.skript.http.server.HttpServer.Response, Troupe>,
                                           stageManager: StageManager<Troupe>,
                                           rule: playwrigkt.skript.http.server.HttpServer.Endpoint): AsyncResult<VertxHttpProduktion<Troupe>> =
            Try {
                requestHandlers
                        .find { it.endpoint.matches(rule) }
                        ?.let { throw HttpError.EndpointAlreadyMatches(it.endpoint, rule) }
                        ?:VertxHttpProduktion(rule, this, skript, stageManager)
            }
                    .map {
                        requestHandlers.add(it)
                        it
                    }
                    .toAsyncResult()

    fun removeHandler(httpEndpoint: playwrigkt.skript.http.server.HttpServer.Endpoint): Try<Unit> =
            Try {
                if(!requestHandlers.removeAll { httpEndpoint.matches(it.endpoint) }) {
                    throw HttpError.EndpointNotHandled(httpEndpoint)
                }
            }

    fun handles(httpEndpoint: playwrigkt.skript.http.server.HttpServer.Endpoint): Boolean =
            requestHandlers.any { it.endpoint.matches(httpEndpoint) }

    override fun tearDown(): AsyncResult<Unit> {
        val result = CompletableResult<Unit>()
        log.info("closing vertx http server")
        server.close(result.vertxHandler())
        return result.map { Unit }
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
}