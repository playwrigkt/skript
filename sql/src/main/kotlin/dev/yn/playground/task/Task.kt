package dev.yn.playground.task

import io.vertx.core.Future
import io.vertx.core.Vertx

interface Task<I, O> {
    companion object {
        fun <I, O> async(f: (I) -> Future<O>) = AsyncTask(f)

        fun <I, O> sync(f: (I) -> O) = SyncTask(f)

        fun <I, O, P: VertxProvider> vertxAsync(vertxAction: (I, Vertx) -> Future<O>, provider: P): Task<I, O> =
                UnpreparedVertxTask<I, O, P>(vertxAction).prepare(provider)

    }
    fun run(i: I): Future<O>

    fun <O2> andThen(task: Task<O, O2>): Task<I, O2> = TaskLink(this, task)

    fun <O2> async(f: (O) -> Future<O2>) = this.andThen(AsyncTask(f))

    fun <O2> sync(f: (O) -> O2) = this.andThen(SyncTask(f))

    fun <O2, P: VertxProvider> vertxAsync(vertxAction: (O, Vertx) -> Future<O2>, provider: P): Task<I, O2> =
            this.andThen(UnpreparedVertxTask<O, O2, P>(vertxAction).prepare(provider))
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

data class VertxTask<I, O>(val vertxAction: (I, Vertx) -> Future<O>, val vertx: Vertx): Task<I, O> {
    override fun run(i: I): Future<O> {
        return vertxAction(i, vertx)
    }
}

interface VertxProvider {
    fun provideVertx(): Vertx
}

interface UnpreparedTask<I, O, in P> {
    fun prepare(p: P): Task<I, O>
}

data class UnpreparedVertxTask<I, O, in P: VertxProvider>(val vertxAction: (I, Vertx) -> Future<O>): UnpreparedTask<I, O, P> {
    override fun prepare(p: P): Task<I, O> {
        return AsyncTask( { vertxAction(it, p.provideVertx()) })
    }
}