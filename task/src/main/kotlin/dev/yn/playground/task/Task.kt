package dev.yn.playground.task

import io.vertx.core.Future
import io.vertx.core.Vertx

/**
 * A head represents a one or more sequential asynchronous actions that can be run as many times as desired.
 *
 * A head is essentially a set of functions that map into each other
 */
interface Task<I, O> {
    companion object {
        fun <I, O, PROVIDER: VertxProvider> vertxAsync(vertxAction: (I, Vertx) -> Future<O>, provider: PROVIDER): Task<I, O> =
                UnpreparedVertxTask<I, O, PROVIDER>(vertxAction).prepare(provider)

    }
    fun run(i: I): Future<O>

    fun <O2> andThen(task: Task<O, O2>): Task<I, O2> = TaskLink(this, task)

    /**
     * A link that contains two tasks that have been chained together.  A chain is essentially a single linked list of tasks.
     */
    private data class TaskLink<I, J, O>(val head: Task<I, J>, val tail: Task<J, O>): Task<I, O> {
        override fun run(i: I): Future<O> {
            return head.run(i).compose(tail::run)
        }

        override fun <O2> andThen(task: Task<O, O2>): Task<I, O2> = TaskLink(this.head, tail.andThen(task))
    }
}