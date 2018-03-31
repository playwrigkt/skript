package playwrigkt.skript.stagemanager

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import playwrigkt.skript.troupe.AMQPPublishTroupe
import playwrigkt.skript.troupe.QueuePublishTroupe

data class AMQPPublishStageManager(
        val exchange: String,
        val connection: Connection,
        val basicProperties: AMQP.BasicProperties): StageManager<QueuePublishTroupe> {
    override fun hireTroupe(): QueuePublishTroupe = AMQPPublishTroupe(exchange, connection, basicProperties)
}