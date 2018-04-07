package playwrigkt.skript.venue

import io.vertx.core.MultiMap
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerRequest
import org.funktionale.option.firstOption
import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.http.*
import playwrigkt.skript.produktion.VertxHttpProduktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.toAsyncResult
import playwrigkt.skript.stagemanager.StageManager

class VertxHttpVenue(val server: HttpServer): HttpVenue {
    private val requestHandlers: MutableList<VertxHttpProduktion<*>> = mutableListOf()

    init {
        server.requestHandler { serverRequest ->
            val method = serverRequest.method().toHttpMethod()
            val headers = serverRequest.headers().toMap()
            val uri = serverRequest.absoluteURI()
            val body = serverRequest.body()

            requestHandlers
                    .firstOption { it.endpoint.matches(method, headers, uri)}
                    .map {produktion -> HttpRequest.of(uri, method, headers, body, path(uri), produktion.endpoint).flatMap(produktion::invoke) }
                    .orNull()
                    ?.map { serverRequest.response().setStatusCode(it.status).end(Buffer.buffer(it.responseBody)) }
                    ?.recover { Try { serverRequest.response().setStatusCode(500).end() }.toAsyncResult() }
                    ?: serverRequest.response().setStatusCode(501).end("Not Implemented")
        }
    }


    private fun path(requestUri: String): String = requestUri.substringBefore("?")

    private fun io.vertx.core.http.HttpMethod.toHttpMethod(): HttpMethod =
            when(this) {
                io.vertx.core.http.HttpMethod.POST -> HttpMethod.Post
                io.vertx.core.http.HttpMethod.GET -> HttpMethod.Get
                io.vertx.core.http.HttpMethod.PUT -> HttpMethod.Put
                io.vertx.core.http.HttpMethod.PATCH-> HttpMethod.Patch
                io.vertx.core.http.HttpMethod.DELETE -> HttpMethod.Delete
                io.vertx.core.http.HttpMethod.HEAD -> HttpMethod.Head
                io.vertx.core.http.HttpMethod.OPTIONS -> HttpMethod.Options
                io.vertx.core.http.HttpMethod.TRACE -> HttpMethod.Trace
                io.vertx.core.http.HttpMethod.CONNECT -> HttpMethod.Connect
                io.vertx.core.http.HttpMethod.OTHER -> HttpMethod.Other("")
            }

    private fun MultiMap.toMap(): Map<String, List<String>> = this.names().map { it to this.getAll(it)}.toMap()

    private fun HttpServerRequest.body(): AsyncResult<ByteArray> {
        val result = CompletableResult<Buffer>()
        this.bodyHandler(result::succeed)
        return result.map { it.bytes }
    }

    override fun <Troupe> produktion(skript: Skript<HttpRequest<ByteArray>, HttpResponse, Troupe>,
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