package playwright.skript.consumer.alpha

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import playwright.skript.Skript
import playwright.skript.result.AsyncResult
import playwright.skript.result.CompletableResult
import playwright.skript.result.Result
import playwright.skript.venue.Venue
import java.util.concurrent.LinkedBlockingQueue

class VertxConsumerStage(val vertx: Vertx): playwright.skript.consumer.alpha.ConsumerStage {
    override fun <C> buildPerformer(target: String, venue: Venue<C>): playwright.skript.consumer.alpha.ConsumerPerformer<C> {
        return VertxConsumerPerformer(target, vertx, venue)
    }

}
class VertxConsumerPerformer<C>(
        val address: String,
        val vertx: Vertx,
        val provider: Venue<C>): playwright.skript.consumer.alpha.ConsumerPerformer<C> {

    override fun <O> stream(skript: Skript<playwright.skript.consumer.alpha.ConsumedMessage, O, C>): AsyncResult<playwright.skript.consumer.alpha.Stream<O>> {
        return AsyncResult.succeeded(VertxConsumeStream(vertx, address, skript, provider))
    }


    override fun <O> sink(skript: Skript<playwright.skript.consumer.alpha.ConsumedMessage, O, C>): AsyncResult<playwright.skript.consumer.alpha.Sink> {
        return AsyncResult.succeeded(VertxConsumeSink(vertx, address, skript, provider))
    }
}

abstract class VertxConsumer<O, C>(
        val vertx: Vertx,
        val address: String,
        val skript: Skript<playwright.skript.consumer.alpha.ConsumedMessage, O, C>,
        val provider: Venue<C>): playwright.skript.consumer.alpha.Consumer {
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

class VertxConsumeSink<O, C>(
        vertx: Vertx,
        address: String,
        skript: Skript<playwright.skript.consumer.alpha.ConsumedMessage, O, C>,
        provider: Venue<C>): playwright.skript.consumer.alpha.Sink, VertxConsumer<O, C>(vertx, address, skript, provider) {

    override fun handleMessage(message: Message<Buffer>) {
        provider.provideStage()
                .flatMap { skript.run(playwright.skript.consumer.alpha.ConsumedMessage(address, message.body().bytes), it) }
                .map {
                    message.reply("success") }
                .recover {
                    message.fail(0, it.message)
                    AsyncResult.failed(it)
                }
    }
}

class VertxConsumeStream<O, C>(
        vertx: Vertx,
        address: String,
        skript: Skript<playwright.skript.consumer.alpha.ConsumedMessage, O, C>,
        provider: Venue<C>): playwright.skript.consumer.alpha.Stream<O>, VertxConsumer<O, C>(vertx, address, skript, provider) {


    override fun handleMessage(message: Message<Buffer>) {
        provider.provideStage()
                .flatMap { skript.run(playwright.skript.consumer.alpha.ConsumedMessage(address, message.body().bytes), it) }
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

