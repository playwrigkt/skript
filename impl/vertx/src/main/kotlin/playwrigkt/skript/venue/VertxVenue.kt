package playwrigkt.skript.venue

import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import org.slf4j.LoggerFactory
import playwrigkt.skript.Skript
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.produktion.VertxProduktion
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.vertx.ex.vertxHandler

class VertxVenue(val vertx: Vertx): QueueVenue {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun teardown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)

    override fun <Troupe> produktion(skript: Skript<QueueMessage, Unit, Troupe>, stageManager: StageManager<Troupe>, rule: String): AsyncResult<Produktion> {
        return AsyncResult.succeeded(VertxProduktion(vertx, rule, skript, stageManager))
    }
}