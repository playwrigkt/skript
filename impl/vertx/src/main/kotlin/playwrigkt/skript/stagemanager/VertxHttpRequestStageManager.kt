package playwrigkt.skript.stagemanager

import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.VertxHttpClientTroupe

data class VertxHttpRequestStageManager(val vertxHttpClientOptions: HttpClientOptions, val vertx: Vertx): StageManager<VertxHttpClientTroupe> {
    private val httpClient by lazy {
        AsyncResult.succeeded(vertx.createHttpClient(vertxHttpClientOptions))
    }

    override fun hireTroupe(): VertxHttpClientTroupe = VertxHttpClientTroupe(httpClient)

    override fun tearDown(): AsyncResult<Unit> {
        return httpClient.map { client ->
            client.close()
        }
    }
}