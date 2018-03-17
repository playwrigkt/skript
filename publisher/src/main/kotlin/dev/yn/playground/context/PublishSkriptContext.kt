package dev.yn.playground.context

import dev.yn.playground.publisher.PublishSkriptExecutor
import dev.yn.playground.result.AsyncResult

interface PublishSkriptContextProvider<E: PublishSkriptExecutor> {
    fun getPublishExecutor(): AsyncResult<E>
}

interface PublishSkriptContext<E: PublishSkriptExecutor> {
    fun getPublishExecutor(): E
}