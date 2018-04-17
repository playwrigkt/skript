package playwrigkt.skript.venue

import io.vertx.core.Vertx
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

    override fun teardown(): AsyncResult<Unit> {
        log.info("closing vertx")
        val result = CompletableResult<Void>()
        vertx.close(result.vertxHandler())
        return result.map { Unit }
    }

    override fun <Troupe> produktion(skript: Skript<QueueMessage, Unit, Troupe>, stageManager: StageManager<Troupe>, rule: String): AsyncResult<Produktion> {
        return AsyncResult.succeeded(VertxProduktion(vertx, rule, skript, stageManager))
    }
}