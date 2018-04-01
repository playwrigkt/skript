package playwrigkt.skript.venue

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import org.funktionale.tries.Try
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult

class AMQPPublishStageManager(
        val exchange: String,
        val connection: Connection,
        val basicProperties: AMQP.BasicProperties): StageManager<playwrigkt.skript.publish.AMQPPublishPerformer> {
    override fun hireTroupe(): AsyncResult<playwrigkt.skript.publish.AMQPPublishPerformer> =
            Try { playwrigkt.skript.publish.AMQPPublishPerformer(exchange, connection.createChannel(), basicProperties) }
                    .toAsyncResult()
}