package playwrigkt.skript.publish

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import org.funktionale.tries.Try
import playwright.skript.performer.QueuePublishPerformer
import playwright.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult

class AMQPPublishPerformer(
        val exchange: String,
        val channel: Channel,
        val basicProperties: AMQP.BasicProperties): QueuePublishPerformer {
    override fun publish(command: QueueMessage): AsyncResult<Unit> {
        return Try {
            channel.basicPublish(exchange, command.source, basicProperties, command.body)
        }.toAsyncResult()
    }
}