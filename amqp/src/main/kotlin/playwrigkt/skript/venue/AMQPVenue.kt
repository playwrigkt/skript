package playwrigkt.skript.venue

import com.rabbitmq.client.Connection
import org.funktionale.tries.Try
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.Skript
import playwrigkt.skript.produktion.AMQPProduktion
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult
import playwrigkt.skript.stagemanager.StageManager

data class AMQPVenue(val amqpConnection: Connection): QueueVenue {
    override fun <O, Troupe> sink(skript: Skript<QueueMessage, O, Troupe>,
                                  stageManager: StageManager<Troupe>,
                                  queue: String): AsyncResult<Produktion> {
        return Try {
            AMQPProduktion(amqpConnection.createChannel(), queue, skript, stageManager)
        }
                .toAsyncResult()
                .map { it as Produktion }
    }
}