package playwrigkt.skript.performer

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult

class AMQPPublishPerformer(
        val exchange: String,
        val channel: Channel,
        val basicProperties: AMQP.BasicProperties): QueuePublishPerformer {
    override fun publish(command: QueueMessage): AsyncResult<Unit> =
        runAsync {  channel.basicPublish(exchange, command.source, basicProperties, command.body) }
}