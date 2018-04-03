package playwrigkt.skript.troupe

import io.vertx.core.Vertx
import playwrigkt.skript.performer.VertxPublishPerformer
import playwrigkt.skript.result.AsyncResult

data class VertxPublishTroupe(val vertx: Vertx): QueuePublishTroupe {
    val performer: AsyncResult<VertxPublishPerformer> by lazy {
        AsyncResult.succeeded(VertxPublishPerformer(vertx.eventBus()))
    }

    override fun getPublishPerformer(): AsyncResult<VertxPublishPerformer> = performer
}