package playwright.skript.venue

import io.vertx.core.Vertx
import playwright.skript.performer.VertxPublishPerformer
import playwright.skript.result.AsyncResult

class VertxPublishVenue(val vertx: Vertx): Venue<VertxPublishPerformer> {
    override fun provideStage(): AsyncResult<VertxPublishPerformer> {
        return AsyncResult.succeeded(VertxPublishPerformer(vertx.eventBus()))
    }

}