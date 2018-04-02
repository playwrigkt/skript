package playwrigkt.skript.venue

import io.vertx.core.Vertx
import playwright.skript.troupe.QueuePublishTroupe
import playwrigkt.skript.performer.VertxPublishPerformer
import playwrigkt.skript.result.AsyncResult

data class VertxPublishStageManager(val vertx: Vertx): StageManager<QueuePublishTroupe> {
    override fun hireTroupe(): QueuePublishTroupe =
        object: QueuePublishTroupe {
            val performer: AsyncResult<VertxPublishPerformer> by lazy {
                AsyncResult.succeeded(VertxPublishPerformer(vertx.eventBus()))
            }

            override fun getPublishPerformer(): AsyncResult<VertxPublishPerformer> = performer.copy()
        }
}