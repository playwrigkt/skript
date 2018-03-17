package dev.yn.playground.publisher

import dev.yn.playground.Skript
import dev.yn.playground.context.PublishTaskContext
import dev.yn.playground.result.AsyncResult

sealed class PublishSkript<I, O>: Skript<I, O, PublishTaskContext<*>> {

    companion object {
        fun<I>publish(mapping: (I) -> PublishCommand.Publish): PublishSkript<I, I> {
            return Publish(mapping)
        }
    }
    data class Publish<I>(val mapping: (I) -> PublishCommand.Publish): PublishSkript<I, I>() {
        override fun run(i: I, context: PublishTaskContext<*>): AsyncResult<I> {
            return context.getPublishExecutor()
                    .publish(mapping(i))
                    .map { i }
        }
    }
}