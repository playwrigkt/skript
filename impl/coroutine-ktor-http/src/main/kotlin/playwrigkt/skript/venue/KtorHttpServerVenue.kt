package playwrigkt.skript.venue

import io.ktor.application.install
import io.ktor.http.HttpMethod
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import org.funktionale.tries.Try
import org.slf4j.LoggerFactory
import playwrigkt.skript.Skript
import playwrigkt.skript.coroutine.ex.suspendMap
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.produktion.KtorHttpServerProduktion
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.LightweightSynchronized
import playwrigkt.skript.stagemanager.StageManager
import java.util.concurrent.TimeUnit

class KtorHttpServerVenue(val port: Int, val maxConnectionMillis: Long): HttpServerVenue, LightweightSynchronized() {
    private val result = CompletableResult<Unit>()
    private val server: NettyApplicationEngine
    private val routing: Routing
    private val log = LoggerFactory.getLogger(this.javaClass)

    init {
        server = embeddedServer(
                Netty,
                configure = {
                    this.responseWriteTimeoutSeconds = (maxConnectionMillis / 1000).toInt()
                },
                port = port) { }

        Try { server.start() }
                .onFailure(result::fail)

        routing = server.application.install(Routing)
   }


    override fun <Troupe> produktion(skript: Skript<HttpServer.Request<ByteArray>, HttpServer.Response, Troupe>, stageManager: StageManager<Troupe>, rule: HttpServer.Endpoint): AsyncResult<out Produktion> =
        AsyncResult.succeeded(Unit)
                .suspendMap { KtorHttpServerProduktion(rule, routing.of(rule), maxConnectionMillis, skript, stageManager) }
                .suspendMap {
                    log.info("ROUTING UPDATED\n${".".repeat(25)}\n${this.routing.printRoute()}\n${".".repeat(25)}")
                    it
                }

    override fun teardown(): AsyncResult<Unit> = lock {
        if(!result.isComplete()) {
            log.info("stopping ktor server")
            Try { server.stop(1000, 1000, TimeUnit.MILLISECONDS) }
                    .onSuccess(result::succeed)
                    .onFailure(result::fail)
            result
        } else {
            log.error("server already stopped!")
            result
        }
    }

    private fun Route.printRoute(depth: Int = 0): String =
        "${" ".repeat(depth)}route: $this\n" + this.children.map { it.printRoute(depth + 1)}.joinToString("\n")


    private fun Routing.of(endpoint: HttpServer.Endpoint): Route = lock {
        this.method(method(endpoint.method)) { }
                .route(endpoint.path) { }
                .addHeaders(endpoint.headers)
    }

    private fun Route.addHeaders(headers: Map<String, List<String>>): Route {
        return headers
                .toList()
                .flatMap { entry -> entry.second.map { entry.first to it } }
                .fold(this) { route, header ->
                    route.header(header.first, header.second) { }
                }
    }

    private fun method(method: Http.Method): HttpMethod {
        return playwrigkt.skript.http.method(method)
    }


}