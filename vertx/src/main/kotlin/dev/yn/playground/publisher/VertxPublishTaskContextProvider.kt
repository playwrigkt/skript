package dev.yn.playground.publisher

import dev.yn.playground.context.PublishSkriptContextProvider
import dev.yn.playground.result.AsyncResult
import io.vertx.core.Vertx

class VertxPublishSkriptContextProvider(val vertx: Vertx): PublishSkriptContextProvider<VertxPublishExecutor> {
    override fun getPublishExecutor(): AsyncResult<VertxPublishExecutor> {
        return AsyncResult.succeeded(VertxPublishExecutor(vertx.eventBus()))
    }

}