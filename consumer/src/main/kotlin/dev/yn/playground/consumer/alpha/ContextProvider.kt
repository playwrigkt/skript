package dev.yn.playground.consumer.alpha

import dev.yn.playground.task.result.AsyncResult

interface ContextProvider<C> {
    fun provideContext(): AsyncResult<C>
}

interface CacheContextProvider<C, R> {
    fun provideContext(r: R): AsyncResult<C>
}