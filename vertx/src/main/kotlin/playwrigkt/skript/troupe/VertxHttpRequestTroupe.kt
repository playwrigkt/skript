package playwrigkt.skript.troupe

import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import playwrigkt.skript.performer.VertxHttpRequestPerformer
import playwrigkt.skript.result.AsyncResult

data class VertxHttpRequestTroupe(val vertxHttpClientOptions: HttpClientOptions, val vertx: Vertx): HttpRequestTroupe {
    private val vertxClient by lazy {
        AsyncResult.succeeded(vertx.createHttpClient(vertxHttpClientOptions))
    }

    override fun getHttpRequestPerformer(): AsyncResult<VertxHttpRequestPerformer> {
       return vertxClient.map { VertxHttpRequestPerformer(it) }
    }

}