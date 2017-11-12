package dev.yn.playground.vertx.publisher

import dev.yn.playground.publisher.PublishTaskContextProvider
import dev.yn.playground.task.result.AsyncResult
import io.vertx.core.Vertx

class VertxPublishTaskContextProvider(val vertx: Vertx): PublishTaskContextProvider<VertxPublishExecutor> {
    override fun getPublishExecutor(): AsyncResult<VertxPublishExecutor> {
        return AsyncResult.succeeded(VertxPublishExecutor(vertx.eventBus()))
    }

}