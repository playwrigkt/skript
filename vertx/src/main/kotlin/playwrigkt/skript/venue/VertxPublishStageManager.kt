package playwrigkt.skript.venue

import io.vertx.core.Vertx
import playwrigkt.skript.performer.VertxPublishPerformer
import playwrigkt.skript.result.AsyncResult

class VertxPublishStageManager(val vertx: Vertx): StageManager<VertxPublishPerformer> {
    override fun hireTroupe(): AsyncResult<VertxPublishPerformer> {
        return AsyncResult.succeeded(VertxPublishPerformer(vertx.eventBus()))
    }

}