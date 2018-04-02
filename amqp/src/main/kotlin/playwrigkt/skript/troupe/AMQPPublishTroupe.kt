package playwrigkt.skript.troupe

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import org.funktionale.tries.Try
import playwrigkt.skript.publish.AMQPPublishPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult

data class AMQPPublishTroupe(val exchange: String,
                             val connection: Connection,
                             val basicProperties: AMQP.BasicProperties): QueuePublishTroupe {
    val performer: AsyncResult<AMQPPublishPerformer> by lazy {
        Try { AMQPPublishPerformer(exchange, connection.createChannel(), basicProperties) }
                .toAsyncResult()
    }
    override fun getPublishPerformer(): AsyncResult<AMQPPublishPerformer> = performer.copy()
}