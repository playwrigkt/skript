package dev.yn.playground.consumer.alpha

import dev.yn.playground.task.Task
import dev.yn.playground.task.result.AsyncResult

interface ConsumerExecutor<C> {
    fun <O> sink(task: Task<ConsumedMessage, O, C>): AsyncResult<Sink>
    fun <O> stream(task: Task<ConsumedMessage, O, C>): AsyncResult<Stream<O>>
}

interface ConsumerContext<C> {
    fun getExecutor(): ConsumerExecutor<C>
}

interface ConsumerContextProvider<C> {
    fun getContext(): AsyncResult<ConsumerContext<C>>
}