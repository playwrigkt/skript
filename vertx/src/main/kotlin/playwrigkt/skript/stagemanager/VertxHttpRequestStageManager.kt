package playwrigkt.skript.stagemanager

import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import playwrigkt.skript.troupe.VertxHttpRequestTroupe

data class VertxHttpRequestStageManager(val vertxHttpClientOptions: HttpClientOptions, val vertx: Vertx): StageManager<VertxHttpRequestTroupe> {
    override fun hireTroupe(): VertxHttpRequestTroupe = VertxHttpRequestTroupe(vertxHttpClientOptions, vertx)
}