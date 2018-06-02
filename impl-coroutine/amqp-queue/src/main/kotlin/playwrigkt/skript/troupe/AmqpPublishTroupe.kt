package playwrigkt.skript.troupe

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.performer.AmqpPublishPerformer
import playwrigkt.skript.result.AsyncResult

data class AmqpPublishTroupe(val exchange: String,
                             val connection: Connection,
                             val basicProperties: AMQP.BasicProperties): QueuePublishTroupe {
    val performer: AsyncResult<AmqpPublishPerformer> by lazy {
        runAsync { AmqpPublishPerformer(exchange, connection.createChannel(), basicProperties) }
    }

    override fun getPublishPerformer(): AsyncResult<AmqpPublishPerformer> = performer
}