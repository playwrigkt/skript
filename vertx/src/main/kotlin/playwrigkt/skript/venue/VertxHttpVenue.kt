package playwrigkt.skript.venue

import io.vertx.core.MultiMap
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerRequest
import org.funktionale.option.firstOption
import org.funktionale.tries.Try
import org.slf4j.LoggerFactory
import playwrigkt.skript.Skript
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
                    ?.map { serverRequest.response().setStatusCode(it.status).end(Buffer.buffer(it.responseBody)) }
                    ?.recover {
                        log.debug("error handling request: {}", it)
                        Try { serverRequest.response().setStatusCode(500).end() }.toAsyncResult()
                    }
                    ?: serverRequest.response().setStatusCode(501).end("Not Implemented")
        }
        server.listen()
    }


    private fun io.vertx.core.http.HttpMethod.toHttpMethod(): Http.Method =
            when(this) {
                io.vertx.core.http.HttpMethod.POST -> Http.Method.Post
                io.vertx.core.http.HttpMethod.GET -> Http.Method.Get
                io.vertx.core.http.HttpMethod.PUT -> Http.Method.Put
                io.vertx.core.http.HttpMethod.PATCH-> Http.Method.Patch
                io.vertx.core.http.HttpMethod.DELETE -> Http.Method.Delete
                io.vertx.core.http.HttpMethod.HEAD -> Http.Method.Head
                io.vertx.core.http.HttpMethod.OPTIONS -> Http.Method.Options
                io.vertx.core.http.HttpMethod.TRACE -> Http.Method.Trace
                io.vertx.core.http.HttpMethod.CONNECT -> Http.Method.Connect
                io.vertx.core.http.HttpMethod.OTHER -> Http.Method.Other("")
            }

    private fun MultiMap.toMap(): Map<String, List<String>> = this.names().map { it to this.getAll(it)}.toMap()

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