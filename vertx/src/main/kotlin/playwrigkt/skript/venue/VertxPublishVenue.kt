package playwrigkt.skript.venue

import io.vertx.core.Vertx
import playwrigkt.skript.performer.VertxPublishPerformer
import playwrigkt.skript.result.AsyncResult

class VertxPublishVenue(val vertx: Vertx): Venue<VertxPublishPerformer> {
    override fun provideStage(): AsyncResult<VertxPublishPerformer> {
        return AsyncResult.succeeded(VertxPublishPerformer(vertx.eventBus()))
    }

}