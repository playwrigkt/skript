package dev.yn.playground.publisher

import dev.yn.playground.Skript
import dev.yn.playground.context.PublishSkriptContext
import dev.yn.playground.result.AsyncResult

sealed class PublishSkript<I, O>: Skript<I, O, PublishSkriptContext<*>> {

    companion object {
        fun<I>publish(mapping: (I) -> PublishCommand.Publish): PublishSkript<I, I> {
            return Publish(mapping)
        }
    }
    data class Publish<I>(val mapping: (I) -> PublishCommand.Publish): PublishSkript<I, I>() {
        override fun run(i: I, context: PublishSkriptContext<*>): AsyncResult<I> {
            return context.getPublishExecutor()
                    .publish(mapping(i))
                    .map { i }
        }
    }
}