package playwrigkt.skript.venue

import io.vertx.core.Vertx
import playwright.skript.queue.QueueMessage
import playwright.skript.venue.QueueVenue
import playwrigkt.skript.Skript
import playwrigkt.skript.produktion.Production
import playwrigkt.skript.produktion.VertxProduction
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager

class VertxVenue(val vertx: Vertx): QueueVenue {
    override fun <Ending, Troupe> sink(skript: Skript<QueueMessage, Ending, Troupe>, stageManager: StageManager<Troupe>, rule: String): AsyncResult<Production> {
        return AsyncResult.succeeded(VertxProduction(vertx, rule, skript, stageManager))
    }
}