package dev.yn.playground.amqp.alpha.consumer

import com.rabbitmq.client.*
import com.rabbitmq.client.impl.AMQConnection
import dev.yn.playground.consumer.alpha.*
import dev.yn.playground.consumer.alpha.Consumer
import dev.yn.playground.task.Task
import dev.yn.playground.task.result.AsyncResult
import dev.yn.playground.task.result.CompletableResult
import dev.yn.playground.task.result.Result
import dev.yn.playground.task.result.toAsyncResult
import org.funktionale.option.Option
import org.funktionale.option.getOrElse
import org.funktionale.tries.Try
import java.util.concurrent.LinkedBlockingQueue

class AMQPConsumerExecutor<C>(
        val amqpConnection: AMQConnection,
        val contextProvider: ContextProvider<C>,
        val queue: String): ConsumerExecutor<C> {

    override fun <O> sink(task: Task<ConsumedMessage, O, C>): AsyncResult<Sink> {
        return Try {
            AMQPSink(amqpConnection.createChannel(), queue, task, contextProvider)
        }
                .toAsyncResult()
                .map { it as Sink }
    }

    override fun <O> stream(task: Task<ConsumedMessage, O, C>): AsyncResult<Stream<O>> {
        return Try {
            AMQPStream(amqpConnection.createChannel(), queue, task, contextProvider)
        }
                .toAsyncResult()
                .map { it as Stream<O> }
    }
}

abstract class AMQPConsumer<O, C>(
        val channel: Channel,
        val queue: String,
        val task: Task<ConsumedMessage, O, C>,
        val provider: ContextProvider<C>): Consumer {
    private val result = CompletableResult<Unit>()
    private val consumerTag: String

    init {
        consumerTag = channel.basicConsume(queue, object: DefaultConsumer(channel) {
            override fun handleDelivery(
                    consumerTag: String,
                    envelope: Envelope,
                    properties: AMQP.BasicProperties,
                    body: ByteArray) {
                handleMessage(consumerTag, envelope, properties, body)
            }
        })
    }

    abstract fun handleMessage(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: ByteArray)

    override fun isRunning(): Boolean {
        return !result.isComplete()
    }

    override fun stop(): AsyncResult<Unit> {
        if(!result.isComplete()) {
            Option.Some(Unit)
                    .filter { channel.isOpen }
                    .flatMap {
                        Try {
                            channel.basicCancel(consumerTag)
                            channel.close()
                        }.toOption()
                    }
                    .map { result.succeed(it) }
                    .getOrElse { result.fail(RuntimeException("TODO")) }
        }
        return result
    }

    override fun result(): AsyncResult<Unit> {
        return result
    }
}
class AMQPSink<O, C>(
        channel: Channel,
        queue: String,
        task: Task<ConsumedMessage, O, C>,
        provider: ContextProvider<C>): Sink, AMQPConsumer<O, C>(channel, queue, task, provider) {
    override fun handleMessage(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: ByteArray) {
        provider.provideContext()
                .flatMap { context -> task.run(ConsumedMessage(envelope.routingKey, body), context) }
                .map { channel.basicAck(envelope.deliveryTag, false) }
                .recover {
                    Try {
                        channel.basicNack(envelope.deliveryTag, false, true)
                    }.toAsyncResult()
                }
    }

}

class AMQPStream<O, C>(
        channel: Channel,
        queue: String,
        task: Task<ConsumedMessage, O, C>,
        provider: ContextProvider<C>): Stream<O>, AMQPConsumer<O, C>(channel, queue, task, provider) {
    override fun handleMessage(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: ByteArray) {
        provider.provideContext()
                .flatMap { context -> task.run(ConsumedMessage(envelope.routingKey, body), context) }
                .enqueue()
                .map { channel.basicAck(envelope.deliveryTag, false) }
                .recover {
                    Try {
                        channel.basicNack(envelope.deliveryTag, false, true)
                    }.toAsyncResult()
                }
    }

    private val results = LinkedBlockingQueue<Result<O>> ()

    override fun hasNext(): Boolean {
        return results.isNotEmpty()
    }

    override fun next(): Result<O> {
        return results.poll()
                ?.let { it }
                ?: Result.Failure(RuntimeException(""))
    }

    private fun AsyncResult<O>.enqueue(): AsyncResult<O> {
        return this
                .map {
                    results.add(Result.Success(it))
                    it
                }
                .recover {
                    results.add(Result.Failure(it))
                    AsyncResult.failed(it)
                }
    }
}
