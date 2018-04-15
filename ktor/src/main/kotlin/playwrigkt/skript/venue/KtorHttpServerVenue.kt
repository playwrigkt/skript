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
import playwrigkt.skript.coroutine.ex.await
import playwrigkt.skript.coroutine.ex.mapSuspend
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
        AsyncResult.succeeded(KtorHttpServerProduktion(rule, this, skript, stageManager))
                .mapSuspend {
                    routeTo(it)
                    log.info("routing updated:\n${routing.printRoute()}")
                    it
                }


    private fun Route.printRoute(depth: Int = 0): String =
        "${" ".repeat(depth)}route: $this\n" + this.children.map { it.printRoute(depth + 1)}.joinToString("\n")

    fun result(): AsyncResult<Unit> = result

    fun stop(): AsyncResult<Unit> =
        lock {
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

    private fun <Troupe> routeTo(produktion: KtorHttpServerProduktion<Troupe>) {
        lock {
            routing.method(method(produktion)) { }
                    .route(produktion.endpoint.path) { }
                    .addHeaders(produktion)
                    .handle { produktion.handle(this.context)
                            .await(maxConnectionMillis)
                            .onFailure {
                                log.info("error processing request: {}", it)
                            } }
        }
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