package dev.yn.playground.context

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import dev.yn.playground.publisher.AMQPPublishSkriptExecutor
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.result.toAsyncResult
import org.funktionale.tries.Try

class AMQPPublishSkriptContextProvider(
        val exchange: String,
        val connection: Connection,
        val basicProperties: AMQP.BasicProperties): PublishSkriptContextProvider<AMQPPublishSkriptExecutor> {
    override fun getPublishExecutor(): AsyncResult<AMQPPublishSkriptExecutor> {
        return Try { AMQPPublishSkriptExecutor(exchange, connection.createChannel(), basicProperties) }.toAsyncResult()
    }

}