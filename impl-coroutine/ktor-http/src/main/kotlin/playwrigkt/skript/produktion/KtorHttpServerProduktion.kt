package playwrigkt.skript.produktion

import io.ktor.application.ApplicationCall
import io.ktor.cio.toByteArray
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.*
import io.ktor.response.header
import io.ktor.response.respondWrite
import io.ktor.routing.Route
import io.ktor.util.toMap
import arrow.core.Try
import org.slf4j.LoggerFactory
import playwrigkt.skript.Skript
import playwrigkt.skript.coroutine.ex.await
import playwrigkt.skript.coroutine.ex.suspendMap
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.LightweightSynchronized
import playwrigkt.skript.stagemanager.StageManager
import java.util.concurrent.locks.ReentrantLock

data class KtorHttpServerProduktion<Troupe>(val endpoint: HttpServer.Endpoint,
                                       val route: Route,
                                       val maxConnectionMillis: Long,
                                       val skript: Skript<HttpServer.Request<ByteArray>, HttpServer.Response, Troupe>,
                                       val stageManager: StageManager<Troupe>): Produktion, LightweightSynchronized {
    override val lock: ReentrantLock = ReentrantLock()
    val log = LoggerFactory.getLogger(this::class.java)
    val result = CompletableResult<Unit>()

    init {
        route.handle {
            handle(this.context)
                    .await(maxConnectionMillis)
                    .fold(
                            { log.info("error processing request: {}", it) },
                            { }
                    )
        }
    }

    override fun isRunning(): Boolean = !result.isComplete()

    override fun stop(): AsyncResult<Unit> = lock {
        if(!result.isComplete()) {
            Try { route.handle {  } }
                    .fold(
                            result::fail,
                            result::succeed
                    )
        } else {
            log.error("endpoint already stopped!")
        }

        result
    }

    override fun result(): AsyncResult<Unit> = result

    private fun handle(call: ApplicationCall): AsyncResult<ApplicationCall> {
        val result = endpoint.request(
                call.request.uri,
                method(call.request.httpMethod),
                call.request.path(),
                call.request.queryString(),
                call.request.headers.toMap(),
                AsyncResult.succeeded(call)
                        .suspendMap { it.receiveChannel().toByteArray() })
                .flatMap { skript.run(it, stageManager.hireTroupe()) }

        result.addHandler {
            log.debug("Handled endpoint $endpoint, producing: $it")
        }
        return result
                .recover(respondError(call))
                .flatMap(respond(call))
    }


    private fun respond(call: ApplicationCall): (HttpServer.Response) -> AsyncResult<ApplicationCall> = { response ->
        call.status(response.status)
                .headers(response.headers)
                .body(response.responseBody)
    }

    private fun <T> respondError(call: ApplicationCall): (Throwable) -> AsyncResult<T> = { error ->
        log.debug("error processing request, {}", error)
        call.status(Http.Status.InternalServerError)
                .body(AsyncResult.succeeded(error.toString().toByteArray()))
                .flatMap { AsyncResult.failed<T>(error) }
    }

    private fun ApplicationCall.status(status: Http.Status): ApplicationCall {
        this.response.status(HttpStatusCode(status.code, status.message))
        return this
    }

    private fun ApplicationCall.headers(headers: Map<String, List<String>>): ApplicationCall {
        headers
                .filterNot { k -> k.key  == "Content-Type"}
                .forEach { key, values ->
                    values.forEach { this.response.header(key, it) }
                }
        return this
    }
    private fun ApplicationCall.body(responseBody: AsyncResult<ByteArray>): AsyncResult<ApplicationCall> =
        responseBody.suspendMap { bytes ->
            this.respondWrite {
                bytes.forEach { write(it.toInt()) }
                flush()
                close()
            }
            this
        }

    private fun method(httpMethod: HttpMethod): Http.Method =
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
}