package playwrigkt.skript.produktion

import com.rabbitmq.client.*
import org.funktionale.tries.Try
import org.slf4j.LoggerFactory
import playwrigkt.skript.Skript
import playwrigkt.skript.coroutine.ex.suspendMap
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.stagemanager.StageManager
import java.util.concurrent.atomic.AtomicBoolean

data class AmqpProduktion<O, Troupe>(
        val channel: Channel,
        val queue: String,
        val skript: Skript<QueueMessage, O, Troupe>,
        val provider: StageManager<Troupe>): Produktion {
    val log = LoggerFactory.getLogger(this::class.java)

    private val result = CompletableResult<Unit>()
    private val stopInitiated = AtomicBoolean(false)

    private val consumerTag: String = channel.basicConsume(
            queue,
            false,
            object: DeliverCallback {
                override fun handle(consumerTag: String, message: Delivery) {
                    skript.run(QueueMessage(message.envelope.routingKey, message.body), provider.hireTroupe())
                            .suspendMap { channel.basicAck(message.envelope.deliveryTag, false) }
                            .recover { error ->
                                runAsync { channel.basicNack(message.envelope.deliveryTag, false, true) }
                                        .flatMap { AsyncResult.failed<Unit>(error) }
                            }
                }
            },
            object: CancelCallback {
                override fun handle(consumerTag: String?) {
                    log.info("Consumer cancelled $consumerTag")
                    if(!stopInitiated.getAndSet(true)) {
                        Try { channel.close() }
                                .onFailure { channel.close() }
                                .onFailure(result::fail)
                                .onSuccess(result::succeed)
                    }
                }
            },
            object: ConsumerShutdownSignalCallback {
                override fun handleShutdownSignal(consumerTag: String?, sig: ShutdownSignalException?) {
                    log.info("received consumer shutdown... $consumerTag $sig")
                    if(!stopInitiated.getAndSet(true)) {
                        Try { result.succeed(Unit) }
                    }
                }

            }
        )


    override fun isRunning(): Boolean {
        return !result.isComplete()
    }

    override fun stop(): AsyncResult<Unit> {
        if(!stopInitiated.getAndSet(true)) {
            val cancelResult = Try { channel.basicCancel(consumerTag) }
                    .onFailure { channel.close() }
                    .map { channel.close() }
                    .onFailure(result::fail)
                    .onSuccess(result::succeed)
            log.info("Cancelled consumer $consumerTag, $cancelResult")
        }
        return result
    }

    override fun result(): AsyncResult<Unit> {
        return result
    }
}
