package playwrigkt.skript.venue

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import playwrigkt.skript.Skript
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.produktion.AmqpProduktion
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager

data class AmqpVenue(val amqpConnectionFactory: ConnectionFactory): QueueVenue() {
    val amqpConnection: Connection by lazy {
        amqpConnectionFactory.newConnection()
    }

    override fun <Troupe> createProduktion(skript: Skript<QueueMessage, Unit, Troupe>,
                                           stageManager: StageManager<Troupe>,
                                           rule: String): AsyncResult<Produktion> {
        return runAsync { AmqpProduktion(amqpConnection.createChannel(), rule, skript, stageManager) }
                .map { it as Produktion }
    }

    override fun tearDown(): AsyncResult<Unit> =
            runAsync { amqpConnection.close() }
}