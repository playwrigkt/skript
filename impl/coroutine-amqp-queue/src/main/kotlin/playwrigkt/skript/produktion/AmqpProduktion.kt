package playwrigkt.skript.produktion

import com.rabbitmq.client.*
import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.coroutine.ex.suspendMap
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.stagemanager.StageManager

data class AmqpProduktion<O, Troupe>(
        val channel: Channel,
        val queue: String,
        val skript: Skript<QueueMessage, O, Troupe>,
        val provider: StageManager<Troupe>): Produktion {
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
        skript.run(QueueMessage(envelope.routingKey, body), provider.hireTroupe())
                .suspendMap { channel.basicAck(envelope.deliveryTag, false) }
                .recover { error ->
                    runAsync { channel.basicNack(envelope.deliveryTag, false, true) }
                            .flatMap { AsyncResult.failed<Unit>(error) }
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
                    .onFailure(result::fail)
                    .onSuccess(result::succeed)
        }
        return result
    }

    override fun result(): AsyncResult<Unit> {
        return result
    }
}
