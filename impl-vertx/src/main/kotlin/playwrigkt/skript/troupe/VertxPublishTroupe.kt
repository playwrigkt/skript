package playwrigkt.skript.troupe

import io.vertx.core.eventbus.EventBus
import playwrigkt.skript.performer.VertxPublishPerformer
import playwrigkt.skript.result.AsyncResult

data class VertxPublishTroupe(val eventBus: EventBus): QueuePublishTroupe {
    val performer: AsyncResult<VertxPublishPerformer> by lazy {
        AsyncResult.succeeded(VertxPublishPerformer(eventBus))
    }

    override fun getPublishPerformer(): AsyncResult<VertxPublishPerformer> = performer
}