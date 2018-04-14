package playwrigkt.skript.produktion

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.request.queryString
import io.ktor.request.uri
import io.ktor.response.header
import io.ktor.response.respondWrite
import io.ktor.util.toMap
import playwrigkt.skript.Skript
import playwrigkt.skript.coroutine.ex.mapSuspend
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.venue.KtorHttpServerVenue

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
                        AsyncResult.succeeded(context.call.request)
                                .mapSuspend { it.receiveContent().inputStream().readAllBytes() })
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