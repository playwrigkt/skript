package playwrigkt.skript.stagemanager

import io.vertx.core.eventbus.EventBus
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.QueuePublishTroupe
import playwrigkt.skript.troupe.VertxPublishTroupe

data class VertxPublishStageManager(val eventBus: EventBus): StageManager<QueuePublishTroupe> {
    override fun hireTroupe(): QueuePublishTroupe = VertxPublishTroupe(eventBus)

    override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
}