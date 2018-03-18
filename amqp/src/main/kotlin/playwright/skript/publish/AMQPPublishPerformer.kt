package playwright.skript.publish

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import org.funktionale.tries.Try
import playwright.skript.performer.PublishCommand
import playwright.skript.performer.PublishPerformer
import playwright.skript.result.AsyncResult
import playwright.skript.result.toAsyncResult

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