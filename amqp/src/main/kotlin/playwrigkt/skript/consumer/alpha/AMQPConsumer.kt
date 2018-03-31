package playwrigkt.skript.consumer.alpha

import com.rabbitmq.client.*
import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.Result
import playwrigkt.skript.result.toAsyncResult
import playwrigkt.skript.venue.Venue
import java.util.concurrent.LinkedBlockingQueue

class AMQPConsumerStage(val amqpConnection: Connection): ConsumerStage<String, ConsumedMessage> {
    override fun <Stage> buildPerformer(target: String, venue: Venue<Stage>): ConsumerPerformer<Stage, ConsumedMessage> {
        return AMQPConsumerPerformer(amqpConnection, venue, target)
    }
}

class AMQPConsumerPerformer<Stage>(
        val amqpConnection: Connection,
        val venue: Venue<Stage>,
        val queue: String): ConsumerPerformer<Stage, ConsumedMessage> {

    override fun <O> sink(skript: Skript<ConsumedMessage, O, Stage>): AsyncResult<Sink> {
        return Try {
            AMQPSink(amqpConnection.createChannel(), queue, skript, venue)
        }
                .toAsyncResult()
                .map { it as Sink }
    }

    override fun <O> stream(skript: Skript<ConsumedMessage, O, Stage>): AsyncResult<Stream<O>> {
        val result = Try {
            val stream = AMQPStream(
                    amqpConnection.createChannel(),
                    queue,
                    skript,
                    venue)
            stream
        }
                .toAsyncResult()
                .map { it as Stream<O> }
        return result
    }
}

abstract class AMQPConsumer<O, Stage>(
        val channel: Channel,
        val queue: String,
        val skript: Skript<ConsumedMessage, O, Stage>,
        val provider: Venue<Stage>): Consumer {
    private val result = CompletableResult<Unit>()
    private val consumerTag: String

    init {
        consumerTag = channel.basicConsume(queue, false, object: DefaultConsumer(channel) {
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
            Try { channel.basicCancel(consumerTag) }
                    .onFailure { channel.close() }
                    .map { channel.close() }
                    .onFailure { result.fail(it) }
                    .onSuccess { result.succeed(it) }
        }
        return result
    }

    override fun result(): AsyncResult<Unit> {
        return result
    }
}
class AMQPSink<O, Stage>(
        channel: Channel,
        queue: String,
        skript: Skript<ConsumedMessage, O, Stage>,
        provider: Venue<Stage>): Sink, AMQPConsumer<O, Stage>(channel, queue, skript, provider) {
    override fun handleMessage(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: ByteArray) {
        provider.provideStage()
                .flatMap { stage -> skript.run(ConsumedMessage(envelope.routingKey, body), stage) }
                .map { channel.basicAck(envelope.deliveryTag, false) }
                .recover {
                    Try {
                        channel.basicNack(envelope.deliveryTag, false, true)
                    }.toAsyncResult()
                }
    }

}

class AMQPStream<O, Stage>(
        channel: Channel,
        queue: String,
        skript: Skript<ConsumedMessage, O, Stage>,
        provider: Venue<Stage>): Stream<O>, AMQPConsumer<O, Stage>(channel, queue, skript, provider) {
    override fun handleMessage(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: ByteArray) {
        provider.provideStage()
                .flatMap { stage -> skript.run(ConsumedMessage(envelope.routingKey, body), stage) }
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
