package dev.yn.playground.task

import io.vertx.core.Future
import io.vertx.core.Vertx

/**
 * A task represents a one or more sequential asynchronous actions that can be run as many times as desired.
 *
 * A task is essentially a set of functions that map into each other
 */
interface Task<I, O> {
    companion object {
        fun <I, O> async(f: (I) -> Future<O>) = AsyncTask(f)

        fun <I, O> sync(f: (I) -> O) = SyncTask(f)

        fun <I, O, PROVIDER: VertxProvider> vertxAsync(vertxAction: (I, Vertx) -> Future<O>, provider: PROVIDER): Task<I, O> =
                UnpreparedVertxTask<I, O, PROVIDER>(vertxAction).prepare(provider)

    }
    fun run(i: I): Future<O>

    fun <O2> andThen(task: Task<O, O2>): Task<I, O2> = TaskLink(this, task)

    fun <O2> async(f: (O) -> Future<O2>) = this.andThen(AsyncTask(f))

    fun <O2> sync(f: (O) -> O2) = this.andThen(SyncTask(f))
}

data class TaskLink<I, J, O>(val task: Task<I, J>, val next: Task<J, O>): Task<I, O> {
    override fun run(i: I): Future<O> {
        return task.run(i)
                .compose(next::run)
    }

    override fun <O2> andThen(task: Task<O, O2>): Task<I, O2> = TaskLink(this.task, next.andThen(task))
}

data class AsyncTask<I, O>(val action: (I) -> Future<O>): Task<I, O> {
    override fun run(i: I): Future<O> {
        return action(i)
    }
}

data class SyncTask<I, O>(val action: (I) -> O): Task<I, O> {
    override fun run(i: I): Future<O> {
        try {
            return Future.succeededFuture(action(i))
        } catch(t: Throwable) {
            return Future.failedFuture(t)
        }
    }
}


