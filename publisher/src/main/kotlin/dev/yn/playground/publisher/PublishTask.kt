package dev.yn.playground.publisher

import dev.yn.playground.task.Task
import dev.yn.playground.task.result.AsyncResult

sealed class PublishTask<I, O, C: PublishTaskContext<*>>: Task<I, O, C> {

    companion object {
        fun<I, C: PublishTaskContext<*>>publish(mapping: (I) -> PublishCommand.Publish): PublishTask<I, I, C> {
            return Publish(mapping)
        }
    }
    data class Publish<I, C: PublishTaskContext<*>>(val mapping: (I) -> PublishCommand.Publish): PublishTask<I, I, C>() {
        override fun run(i: I, context: C): AsyncResult<I> {
            return context.getPublishExecutor()
                    .publish(mapping(i))
                    .map { i }
        }
    }
}