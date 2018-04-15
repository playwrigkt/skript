package playwrigkt.skript.stagemanager

import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import playwrigkt.skript.troupe.VertxHttpClientTroupe

data class VertxHttpRequestStageManager(val vertxHttpClientOptions: HttpClientOptions, val vertx: Vertx): StageManager<VertxHttpClientTroupe> {
    override fun hireTroupe(): VertxHttpClientTroupe = VertxHttpClientTroupe(vertxHttpClientOptions, vertx)
}