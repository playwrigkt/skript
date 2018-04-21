package playwrigkt.skript.troupe

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import org.funktionale.tries.Try
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.performer.AMQPPublishPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult

data class AMQPPublishTroupe(val exchange: String,
                             val connection: Connection,
                             val basicProperties: AMQP.BasicProperties): QueuePublishTroupe {
    val performer: AsyncResult<AMQPPublishPerformer> by lazy {
        runAsync { AMQPPublishPerformer(exchange, connection.createChannel(), basicProperties) }
    }

    override fun getPublishPerformer(): AsyncResult<AMQPPublishPerformer> = performer
}