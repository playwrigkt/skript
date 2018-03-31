package playwrigkt.skript.troupe

import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import playwrigkt.skript.performer.VertxHttpClientPerformer
import playwrigkt.skript.result.AsyncResult

data class VertxHttpClientTroupe(val vertxHttpClientOptions: HttpClientOptions, val vertx: Vertx): HttpClientTroupe {
    private val vertxClient by lazy {
        AsyncResult.succeeded(vertx.createHttpClient(vertxHttpClientOptions))
    }

    override fun getHttpRequestPerformer(): AsyncResult<VertxHttpClientPerformer> {
       return vertxClient.map { VertxHttpClientPerformer(it) }
    }

}