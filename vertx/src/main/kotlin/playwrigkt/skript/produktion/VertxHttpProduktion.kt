package playwrigkt.skript.produktion

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.http.HttpEndpoint
import playwrigkt.skript.http.HttpError
import playwrigkt.skript.http.HttpRequest
import playwrigkt.skript.http.HttpResponse
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.toAsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.venue.VertxHttpVenue

class VertxHttpProduktion<Troupe>(
        val endpoint: HttpEndpoint,
        val httpVenue: VertxHttpVenue,
        val skript: Skript<HttpRequest, HttpResponse, Troupe>,
        val provider: StageManager<Troupe>
): Produktion {
    private val completer = CompletableResult<Unit>()
    private val result = completer
            .flatMap { httpVenue.removeHandler(endpoint).toAsyncResult() }

    fun invoke(httpRequest: HttpRequest): AsyncResult<HttpResponse> {
        return skript.run(httpRequest, provider.hireTroupe());
    }

    override fun isRunning(): Boolean =
        httpVenue.handles(this.endpoint)

    override fun stop(): AsyncResult<Unit> {
        if(!completer.isComplete()) {
            completer.succeed(Unit)
            return result
        } else {
            return AsyncResult.failed(HttpError.AlreadyStopped)
        }

    }

    override fun result(): AsyncResult<Unit> = result

}