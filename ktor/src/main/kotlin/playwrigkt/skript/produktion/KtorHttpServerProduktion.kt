package playwrigkt.skript.produktion

import io.ktor.application.ApplicationCall
import io.ktor.cio.toByteArray
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.*
import io.ktor.response.header
import io.ktor.response.respondWrite
import io.ktor.util.toMap
import org.slf4j.LoggerFactory
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
    val log = LoggerFactory.getLogger(this::class.java)

    fun handle(call: ApplicationCall): AsyncResult<ApplicationCall> {

        val result = endpoint.request(
                call.request.uri,
                method(call.request.httpMethod),
                call.request.path(),
                call.request.queryString(),
                call.request.headers.toMap(),
                AsyncResult.succeeded(call)
                        .mapSuspend { it.receiveChannel().toByteArray() })
                .flatMap { skript.run(it, stageManager.hireTroupe()) }

        result.addHandler {
            log.debug("Handled endpoint $endpoint, producing: $it")
        }
        return result
                .recover(respondError(call))
                .flatMap(respond(call))
    }


    fun respond(call: ApplicationCall): (HttpServer.Response) -> AsyncResult<ApplicationCall> = { response ->
        call.status(response.status)
                .headers(response.headers)
                .body(response.responseBody)
    }

    fun <T> respondError(call: ApplicationCall): (Throwable) -> AsyncResult<T> = { error ->
        log.debug("error processing request, {}", error)
        call.status(Http.Status.InternalServerError)
                .body(AsyncResult.succeeded(error.toString().toByteArray()))
                .flatMap { AsyncResult.failed<T>(error) }
    }

    fun ApplicationCall.status(status: Http.Status): ApplicationCall {
        this.response.status(HttpStatusCode(status.code, status.message))
        return this
    }

    fun ApplicationCall.headers(headers: Map<String, List<String>>): ApplicationCall {
        headers
                .filterNot { k -> k.key  == "Content-Type"}
                .forEach { key, values ->
                    values.forEach { this.response.header(key, it) }
                }
        return this
    }
    fun ApplicationCall.body(responseBody: AsyncResult<ByteArray>): AsyncResult<ApplicationCall> =
        responseBody.mapSuspend { bytes ->
            this.respondWrite() {
                bytes.forEach { write(it.toInt()) }
                flush()
                close()
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