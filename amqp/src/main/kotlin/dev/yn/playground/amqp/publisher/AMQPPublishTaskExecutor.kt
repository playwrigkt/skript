package dev.yn.playground.amqp.publisher

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import dev.yn.playground.publisher.PublishCommand
import dev.yn.playground.publisher.PublishTaskContextProvider
import dev.yn.playground.publisher.PublishTaskExecutor
import dev.yn.playground.task.result.AsyncResult
import dev.yn.playground.task.result.toAsyncResult
import org.funktionale.tries.Try
class AMQPPublishTaskContextProvider(
        val exchange: String,
        val connection: Connection,
        val basicProperties: AMQP.BasicProperties): PublishTaskContextProvider<AMQPPublishTaskExecutor> {
    override fun getPublishExecutor(): AsyncResult<AMQPPublishTaskExecutor> {
        return Try { AMQPPublishTaskExecutor(exchange, connection.createChannel(), basicProperties) }.toAsyncResult()
    }

}
class AMQPPublishTaskExecutor(
        val exchange: String,
        val channel: Channel,
        val basicProperties: AMQP.BasicProperties): PublishTaskExecutor {
    override fun publish(command: PublishCommand.Publish): AsyncResult<Unit> {
        return Try {
            channel.basicPublish(exchange, command.target, basicProperties, command.body)
        }.toAsyncResult()
    }
}