package playwrigkt.skript.consumer.alpha

import com.rabbitmq.client.*
import org.funktionale.tries.Try
import playwright.skript.consumer.alpha.QueueMessage
import playwright.skript.consumer.alpha.QueueVenue
import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.toAsyncResult
import playwrigkt.skript.venue.StageManager

data class AMQPVenue(val amqpConnection: Connection): QueueVenue {
    override fun <O, Troupe> sink(skript: Skript<QueueMessage, O, Troupe>,
                                  stageManager: StageManager<Troupe>,
                                  queue: String): AsyncResult<Production> {
        return Try {
            AMQPProduction(amqpConnection.createChannel(), queue, skript, stageManager)
        }
                .toAsyncResult()
                .map { it as Production }
    }
}

data class AMQPProduction<O, Troupe>(
        val channel: Channel,
        val queue: String,
        val skript: Skript<QueueMessage, O, Troupe>,
        val provider: StageManager<Troupe>): Production {
    private val result = CompletableResult<Unit>()
    private val consumerTag: String = channel.basicConsume(queue, false, object: DefaultConsumer(channel) {
            override fun handleDelivery(
                    consumerTag: String,
                    envelope: Envelope,
                    properties: AMQP.BasicProperties,
                    body: ByteArray) {
                handleMessage(consumerTag, envelope, properties, body)
            }
        })


    fun handleMessage(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: ByteArray) {
        provider.hireTroupe()
                .flatMap { stage -> skript.run(QueueMessage(envelope.routingKey, body), stage) }
                .map { channel.basicAck(envelope.deliveryTag, false) }
                .recover {
                    Try {
                        channel.basicNack(envelope.deliveryTag, false, true)
                    }.toAsyncResult()
                }
    }

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
