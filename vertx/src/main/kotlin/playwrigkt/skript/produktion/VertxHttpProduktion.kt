package playwrigkt.skript.produktion

import playwrigkt.skript.Skript
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.HttpError
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.toAsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.venue.VertxHttpServerVenue

class VertxHttpProduktion<Troupe>(
        val endpoint: Http.Server.Endpoint,
        val httpServerVenue: VertxHttpServerVenue,
        val skript: Skript<Http.Server.Request<ByteArray>, Http.Server.Response, Troupe>,
        val provider: StageManager<Troupe>
): Produktion {
    private val completer = CompletableResult<Unit>()
    private val result = completer
            .flatMap { httpServerVenue.removeHandler(endpoint).toAsyncResult() }

    fun invoke(httpServerRequest: Http.Server.Request<ByteArray>): AsyncResult<Http.Server.Response> {
        return skript.run(httpServerRequest, provider.hireTroupe());
    }

    override fun isRunning(): Boolean =
        httpServerVenue.handles(this.endpoint)

    override fun stop(): AsyncResult<Unit> {
        if(!completer.isComplete()) {
            completer.succeed(Unit)
            return result
        } else {
            return AsyncResult.failed(HttpError.AlreadyStopped  )
        }

    }

    override fun result(): AsyncResult<Unit> = result

}