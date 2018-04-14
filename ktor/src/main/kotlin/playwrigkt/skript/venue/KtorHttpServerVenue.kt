package playwrigkt.skript.venue

import playwrigkt.skript.Skript
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.funktionale.tries.Try
import playwrigkt.skript.coroutine.ex.mapSuspend
import playwrigkt.skript.http.HttpError
import playwrigkt.skript.produktion.KtorHttpServerProduktion
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.LightweightSynchronized
import java.util.concurrent.TimeUnit

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
        return playwrigkt.skript.http.method(produktion.endpoint.method)
    }
}