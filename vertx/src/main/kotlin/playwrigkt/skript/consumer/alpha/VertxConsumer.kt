package playwrigkt.skript.consumer.alpha

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import playwright.skript.consumer.alpha.QueueConsumerProduction
import playwright.skript.consumer.alpha.QueueConsumerTroupe
import playwright.skript.consumer.alpha.QueueMessage
import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.Result
import playwrigkt.skript.venue.Venue
import java.util.concurrent.LinkedBlockingQueue

class VertxConsumerTroupe(val vertx: Vertx): QueueConsumerTroupe {
    override fun <Stage> production(target: String, venue: Venue<Stage>): VertxConsumerProduction<Stage> {
        return VertxConsumerProduction(target, vertx, venue)
    }
}

class VertxConsumerProduction<Stage>(
        val address: String,
        val vertx: Vertx,
        val provider: Venue<Stage>): QueueConsumerProduction<Stage> {

    override fun <O> stream(skript: Skript<QueueMessage, O, Stage>): AsyncResult<Stream<O>> {
        return AsyncResult.succeeded(VertxConsumeStream(vertx, address, skript, provider))
    }


    override fun <O> sink(skript: Skript<QueueMessage, O, Stage>): AsyncResult<Sink> {
        return AsyncResult.succeeded(VertxConsumeSink(vertx, address, skript, provider))
    }
}

abstract class VertxConsumer<O, Stage>(
        val vertx: Vertx,
        val address: String,
        val skript: Skript<QueueMessage, O, Stage>,
        val provider: Venue<Stage>): Consumer {
    private val result: CompletableResult<Unit> = CompletableResult()
    private val verticle: AbstractVerticle

    init {
        val handler = this::handleMessage
        verticle = object: AbstractVerticle() {
            override fun toString(): String = "VertxConsumer-$address"

            override fun start() {
                vertx.eventBus().consumer<Buffer>(address, handler)
            }
        }
        vertx.deployVerticle(verticle)
    }

    protected abstract fun handleMessage(message: Message<Buffer>)

    override fun isRunning(): Boolean {
        return !result.isComplete()
    }

    override fun stop(): AsyncResult<Unit> {
        if(!result.isComplete()) {
            vertx.undeploy(
                    verticle.deploymentID(),
                    {
                        if(it.succeeded()) {
                            result.succeed(Unit)
                        } else {
                            result.fail(it.cause())
                        }
                    })
        }
        return result
    }

    override fun result(): AsyncResult<Unit> = result
}

class VertxConsumeSink<O, Stage>(
        vertx: Vertx,
        address: String,
        skript: Skript<QueueMessage, O, Stage>,
        provider: Venue<Stage>): Sink, VertxConsumer<O, Stage>(vertx, address, skript, provider) {

    override fun handleMessage(message: Message<Buffer>) {
        provider.provideStage()
                .flatMap { skript.run(QueueMessage(address, message.body().bytes), it) }
                .map {
                    message.reply("success") }
                .recover {
                    message.fail(0, it.message)
                    AsyncResult.failed(it)
                }
    }
}

class VertxConsumeStream<O, Stage>(
        vertx: Vertx,
        address: String,
        skript: Skript<QueueMessage, O, Stage>,
        provider: Venue<Stage>): Stream<O>, VertxConsumer<O, Stage>(vertx, address, skript, provider) {


    override fun handleMessage(message: Message<Buffer>) {
        provider.provideStage()
                .flatMap { skript.run(QueueMessage(address, message.body().bytes), it) }
                .enqueue()
                .map { message.reply("success") }
                .recover {
                    message.fail(0, it.message)
                    AsyncResult.failed(it)
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

