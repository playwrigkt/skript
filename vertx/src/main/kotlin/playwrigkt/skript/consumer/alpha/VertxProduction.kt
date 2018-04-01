package playwrigkt.skript.consumer.alpha

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import playwright.skript.consumer.alpha.QueueMessage
import playwright.skript.consumer.alpha.QueueVenue
import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.venue.StageManager

class VertxVenue(val vertx: Vertx): QueueVenue {
    override fun <Ending, Troupe> sink(skript: Skript<QueueMessage, Ending, Troupe>, stageManager: StageManager<Troupe>, rule: String): AsyncResult<Production> {
        return AsyncResult.succeeded(VertxProduction(vertx, rule, skript, stageManager))
    }
}

data class VertxProduction<O, Troupe>(
        val vertx: Vertx,
        val address: String,
        val skript: Skript<QueueMessage, O, Troupe>,
        val provider: StageManager<Troupe>): Production {
    private val result: CompletableResult<Unit> = CompletableResult()
    private val verticle: AbstractVerticle

    init {
        val handler = this::handleMessage
        verticle = object: AbstractVerticle() {
            override fun toString(): String = "VertxProduction-$address"

            override fun start() {
                vertx.eventBus().consumer<Buffer>(address, handler)
            }
        }
        vertx.deployVerticle(verticle)
    }

    fun handleMessage(message: Message<Buffer>) {
        provider.hireTroupe()
                .flatMap { skript.run(QueueMessage(address, message.body().bytes), it) }
                .map {
                    message.reply("success") }
                .recover {
                    message.fail(0, it.message)
                    AsyncResult.failed(it)
                }
    }

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
