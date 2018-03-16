package dev.yn.playground.amqp.publisher

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import dev.yn.playground.publisher.PublishCommand
import dev.yn.playground.publisher.PublishTaskExecutor
import dev.yn.playground.task.result.AsyncResult
import dev.yn.playground.task.result.toAsyncResult
import org.funktionale.tries.Try

class AMQPPublishExecutor(
        val exchange: String,
        val channel: Channel,
        val basicProperties: AMQP.BasicProperties): PublishTaskExecutor {
    override fun publish(command: PublishCommand.Publish): AsyncResult<Unit> {
        return Try {
            channel.basicPublish(exchange, command.target, basicProperties, command.body)
        }.toAsyncResult()
    }
}