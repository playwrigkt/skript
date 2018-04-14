package playwrigkt.skript.http

import playwrigkt.skript.Skript
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.venue.HttpServerVenue
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.pipeline.PipelineContext
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.toMap
import org.funktionale.tries.Try
import playwrigkt.skript.coroutine.ex.mapSuspend
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.LightweightSynchronized
import java.util.concurrent.TimeUnit

class KtorHttpServerProduktion<Troupe>(val endpoint: HttpServer.Endpoint,
                                       val httpServerVenue: KtorHttpServerVenue,
                                       val skript: Skript<HttpServer.Request<ByteArray>, HttpServer.Response, Troupe>,
                                       val stageManager: StageManager<Troupe>): Produktion {

    fun handle(context: PipelineContext<Unit, ApplicationCall>) {
        endpoint
                .request(
                        context.call.request.uri,
                        method(context.call.request.httpMethod),
                        context.call.request.path(),
                        context.call.request.queryString(),
                        context.call.request.headers.toMap(),
                        //TODO use channel, read asynchronously
                        AsyncResult.succeeded(context.call.request.receiveContent().inputStream().readAllBytes()))
                .flatMap { skript.run(it, stageManager.hireTroupe()) }
                .flatMap(respond(context))
    }

    fun respond(context: PipelineContext<Unit, ApplicationCall>): (HttpServer.Response) -> AsyncResult<Unit> = { response ->
            context.call
                    .status(response.status)
                    .headers(response.headers)
                    .body(response.responseBody)
                    .map { Unit }
        }

    fun ApplicationCall.status(status: Http.Status): ApplicationCall {
        this.response.status(HttpStatusCode(status.code, status.message))
        return this
    }

    fun ApplicationCall.headers(headers: Map<String, List<String>>): ApplicationCall {
        headers.forEach { key, values ->
            values.forEach { this.response.header(key, it) }
        }
        return this
    }
    fun ApplicationCall.body(responseBody: AsyncResult<ByteArray>): AsyncResult<ApplicationCall> =
        responseBody.mapSuspend { bytes ->
            this.respondWrite {
                bytes.forEach { write(it.toInt()) }
            }
            this
        }

    fun method(httpMethod: HttpMethod): Http.Method =
            when(httpMethod) {
                HttpMethod.Get -> Http.Method.Get
                HttpMethod.Head -> Http.Method.Head
                HttpMethod.Post -> Http.Method.Post
                HttpMethod.Put -> Http.Method.Put
                HttpMethod.Options -> Http.Method.Options
                HttpMethod.Patch -> Http.Method.Patch
                HttpMethod.Delete -> Http.Method.Delete
                else -> Http.Method.Other(httpMethod.value)
            }

    override fun isRunning(): Boolean = !httpServerVenue.result().isComplete()

    override fun stop(): AsyncResult<Unit> = httpServerVenue.stop()

    override fun result(): AsyncResult<Unit> = httpServerVenue.result()
}

class KtorHttpServerVenue(val port: Int): HttpServerVenue, LightweightSynchronized() {
    private val routing: Routing
    private val server: NettyApplicationEngine
    private val result = CompletableResult<Unit>()

    init {

        server = embeddedServer(Netty, port) { }

        routing = Routing(server.application)

        Try { server.start(wait = true) }
                .onFailure(result::fail)
    }


    override fun <Troupe> produktion(skript: Skript<HttpServer.Request<ByteArray>, HttpServer.Response, Troupe>, stageManager: StageManager<Troupe>, rule: HttpServer.Endpoint): AsyncResult<out Produktion> =
        AsyncResult.succeeded(KtorHttpServerProduktion(rule, this, skript, stageManager))
                .mapSuspend {
                    this.routeTo(it)
                    it
                }


    fun result(): AsyncResult<Unit> = result

    fun stop(): AsyncResult<Unit> =
        lock {
            if(!result.isComplete()) {
                Try { server.stop(1000, 5000, TimeUnit.SECONDS) }
                        .onSuccess(result::succeed)
                        .onFailure(result::fail)
            }
            AsyncResult.failed(HttpError.AlreadyStopped)
        }

    private fun <Troupe> routeTo(produktion: KtorHttpServerProduktion<Troupe>) {
       routing
               .method(method(produktion)) {}
                .route(produktion.endpoint.path) { }
                .addHeaders(produktion)
                .handle { produktion.handle(this) }
    }

    private fun Route.addHeaders(rule: KtorHttpServerProduktion<*>): Route {
        return rule.endpoint.headers
                .toList()
                .flatMap { entry -> entry.second.map { entry.first to it } }
                .fold(this) { route, header ->
                    route.header(header.first, header.second) { }
                }
    }

    private fun method(produktion: KtorHttpServerProduktion<*>): HttpMethod {
        return method(produktion.endpoint.method)
    }
}