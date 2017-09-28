package dev.yn.playground.task

import dev.yn.playground.task.session.SessionOperation
import dev.yn.playground.task.session.SessionTask
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.Json

/**
 * interface for providing vertx instance to VertxTasks
 */
interface VertxProvider {
    fun provideVertx(): Vertx
}

/**
 * A vertx Task that needs to have vertx injected before it can be run
 */
data class UnpreparedVertxTask<I, O, PROVIDER: VertxProvider>(val vertxAction: (I, Vertx) -> Future<O>): UnpreparedTask<I, O, PROVIDER> {
    override fun prepare(p: PROVIDER): Task<I, O> {
        return PreparedTask(vertxAction, p.provideVertx())
    }
}

fun <I, O, O2, PROVIDER: VertxProvider> UnpreparedTask<I, O, PROVIDER>.vertxAsync(vertxAction: (O, Vertx) -> Future<O2>): UnpreparedTask<I, O2, PROVIDER> =
        this.andThen(UnpreparedVertxTask(vertxAction))

object VertxTask {
    fun <I, O, PROVIDER: VertxProvider> vertxAsync(vertxAction: (I, Vertx) -> Future<O>, provider: PROVIDER): Task<I, O> =
            UnpreparedVertxTask<I, O, PROVIDER>(vertxAction).prepare(provider)

    fun <T> sendWithResponse(address: String) = { t: T, vertx: Vertx->
        val future = Future.future<Message<String>>()
        vertx.eventBus().send(address, Json.encode(t), future.completer())
        future.map { t }
    }

    fun <T> sendAndForget(address: String) = { t: T, vertx: Vertx ->
        vertx.eventBus().send(address, Json.encode(t))
        Future.succeededFuture(t)
    }
}