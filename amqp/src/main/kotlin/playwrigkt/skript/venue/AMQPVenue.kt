package playwrigkt.skript.venue

import com.rabbitmq.client.Connection
import org.funktionale.tries.Try
import playwright.skript.queue.QueueMessage
import playwright.skript.venue.QueueVenue
import playwrigkt.skript.Skript
import playwrigkt.skript.produktion.AMQPProduction
import playwrigkt.skript.produktion.Production
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult
import playwrigkt.skript.stagemanager.StageManager

data class AMQPVenue(val amqpConnection: Connection): QueueVenue {
    override fun <O, Troupe> sink(skript: Skript<QueueMessage, O, Troupe>,
                                  stageManager: StageManager<Troupe>,
                                  queue: String): AsyncResult<Production> {
        return Try {
            AMQPProduction(amqpConnection.createChannel(), queue, skript, stageManager)
        }
                .toAsyncResult()
                .map { it as Production }
    }
}