package dev.yn.playground.task

import dev.yn.playground.sql.SQLTransaction
import dev.yn.playground.sql.SQLTransactionExecutor
import io.vertx.core.Future
import io.vertx.core.Vertx

interface Task<I, O> {
    companion object {
        fun <I, O> async(f: (I) -> Future<O>) = AsyncTask(f)

        fun <I, O> sync(f: (I) -> O) = SyncTask(f)

        fun <I, O, P: SQLTransactionExecutorProvider> sqlUpdate(transaction: SQLTransaction<I, O>, provider: P) =
                UnpreparedSQLUpdateTask<I, O, P>(transaction).prepare(provider)

        fun <I, O, P: SQLTransactionExecutorProvider> sqlQuery(transaction: SQLTransaction<I, O>, provider: P) =
                UnpreparedSQLQueryTask<I, O, P>(transaction).prepare(provider)

        fun <I, O, P: SQLTransactionExecutorProvider> sqlExec(transaction: SQLTransaction<I, O>, provider: P): Task<I, O> =
                UnpreparedSQLExecTask<I, O, P>(transaction).prepare(provider)

        fun <I, O, P: VertxProvider> vertxAsync(vertxAction: (I, Vertx) -> Future<O>, provider: P): Task<I, O> =
                UnpreparedVertxTask<I, O, P>(vertxAction).prepare(provider)

    }
    fun run(i: I): Future<O>

    fun <O2> andThen(task: Task<O, O2>): Task<I, O2> = TaskLink(this, task)

    fun <O2> async(f: (O) -> Future<O2>) = this.andThen(AsyncTask(f))

    fun <O2> sync(f: (O) -> O2) = this.andThen(SyncTask(f))

    fun <O2, P: SQLTransactionExecutorProvider> sqlUpdate(transaction: SQLTransaction<O, O2>, provider: P) =
            this.andThen(UnpreparedSQLUpdateTask<O, O2, P>(transaction).prepare(provider))

    fun <O2, P: SQLTransactionExecutorProvider> sqlQuery(transaction: SQLTransaction<O, O2>, provider: P) =
            this.andThen(UnpreparedSQLQueryTask<O, O2, P>(transaction).prepare(provider))

    fun <O2, P: SQLTransactionExecutorProvider> sqlExec(transaction: SQLTransaction<O, O2>, provider: P): Task<I, O2> =
            this.andThen(UnpreparedSQLExecTask<O, O2, P>(transaction).prepare(provider))

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

interface SQLTransactionExecutorProvider {
    fun provideSQLTransactionExecutor(): SQLTransactionExecutor
}
interface UnpreparedTask<I, O, in P> {
    fun prepare(p: P): Task<I, O>
}

data class UnpreparedVertxTask<I, O, in P: VertxProvider>(val vertxAction: (I, Vertx) -> Future<O>): UnpreparedTask<I, O, P> {
    override fun prepare(p: P): Task<I, O> {
        return AsyncTask( { vertxAction(it, p.provideVertx()) })
    }
}

data class UnpreparedSQLQueryTask<I, O, in P: SQLTransactionExecutorProvider>(val transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, P>{
    override fun prepare(p: P): Task<I, O> {
        return AsyncTask( { p.provideSQLTransactionExecutor().query(it, transaction) })
    }
}

data class UnpreparedSQLUpdateTask<I, O, in P: SQLTransactionExecutorProvider>(val transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, P>{
    override fun prepare(p: P): Task<I, O> {
        return AsyncTask( { p.provideSQLTransactionExecutor().update(it, transaction) })
    }
}

data class UnpreparedSQLExecTask<I, O, in P: SQLTransactionExecutorProvider>(val transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, P> {
    override fun prepare(p: P): Task<I, O> {
        return AsyncTask({ p.provideSQLTransactionExecutor().execute(it, transaction)})
    }
}

