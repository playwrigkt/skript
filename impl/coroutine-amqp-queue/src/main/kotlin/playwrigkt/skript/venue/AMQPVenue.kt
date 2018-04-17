package playwrigkt.skript.venue

import com.rabbitmq.client.Connection
import org.funktionale.tries.Try
import org.slf4j.LoggerFactory
import playwrigkt.skript.Skript
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.produktion.AMQPProduktion
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult
import playwrigkt.skript.stagemanager.StageManager

//TODO passin connection config
data class AMQPVenue(val amqpConnection: Connection): QueueVenue {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun <Troupe> produktion(skript: Skript<QueueMessage, Unit, Troupe>,
                                        stageManager: StageManager<Troupe>,
                                        queue: String): AsyncResult<Produktion> {
        return runAsync { AMQPProduktion(amqpConnection.createChannel(), queue, skript, stageManager) }
                .map { it as Produktion }
    }

    override fun teardown(): AsyncResult<Unit> =
            runAsync {
                log.info("closing amqp connection")
                amqpConnection.close()
            }
}