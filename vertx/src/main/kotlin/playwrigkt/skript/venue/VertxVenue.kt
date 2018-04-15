package playwrigkt.skript.venue

import io.vertx.core.Vertx
import playwrigkt.skript.Skript
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.produktion.VertxProduktion
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager

class VertxVenue(val vertx: Vertx): QueueVenue {
    override fun <Troupe> produktion(skript: Skript<QueueMessage, Unit, Troupe>, stageManager: StageManager<Troupe>, rule: String): AsyncResult<Produktion> {
        return AsyncResult.succeeded(VertxProduktion(vertx, rule, skript, stageManager))
    }
}