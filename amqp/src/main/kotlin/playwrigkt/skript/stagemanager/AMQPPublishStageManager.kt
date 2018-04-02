package playwrigkt.skript.stagemanager

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import org.funktionale.tries.Try
import playwrigkt.skript.troupe.QueuePublishTroupe
import playwrigkt.skript.publish.AMQPPublishPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult

data class AMQPPublishStageManager(
        val exchange: String,
        val connection: Connection,
        val basicProperties: AMQP.BasicProperties): StageManager<QueuePublishTroupe> {
    override fun hireTroupe(): QueuePublishTroupe =
            object: QueuePublishTroupe {
                val performer: AsyncResult<AMQPPublishPerformer> by lazy {
                    Try { playwrigkt.skript.publish.AMQPPublishPerformer(exchange, connection.createChannel(), basicProperties) }
                            .toAsyncResult()
                }
                override fun getPublishPerformer(): AsyncResult<AMQPPublishPerformer> = performer.copy()
            }
}