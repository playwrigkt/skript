package playwrigkt.skript.publish

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import org.funktionale.tries.Try
import playwrigkt.skript.performer.PublishCommand
import playwrigkt.skript.performer.PublishPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult

class AMQPPublishPerformer(
        val exchange: String,
        val channel: Channel,
        val basicProperties: AMQP.BasicProperties): PublishPerformer {
    override fun publish(command: PublishCommand.Publish): AsyncResult<Unit> {
        return Try {
            channel.basicPublish(exchange, command.target, basicProperties, command.body)
        }.toAsyncResult()
    }
}