package playwrigkt.skript.stagemanager

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.ConnectionFactory
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.AmqpPublishTroupe
import playwrigkt.skript.troupe.QueuePublishTroupe

data class AmqpPublishStageManager(
        val exchange: String,
        val connectionFactory: ConnectionFactory,
        val basicProperties: AMQP.BasicProperties): StageManager<QueuePublishTroupe> {
    val amqpConnection by lazy {
        connectionFactory.newConnection()
    }

    override fun hireTroupe(): QueuePublishTroupe = AmqpPublishTroupe(exchange, amqpConnection, basicProperties)

    override fun tearDown(): AsyncResult<Unit> {
        return runAsync { amqpConnection.close() }
    }
}