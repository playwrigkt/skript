package dev.yn.playground.task

import dev.yn.playground.sql.SQLTransaction
import dev.yn.playground.sql.SQLTransactionExecutor
import io.vertx.core.Future
import io.vertx.core.Vertx

interface Task<I, O> {
    fun run(i: I): Future<O>

    fun <O2> andThen(task: Task<O, O2>): Task<I, O2> = TaskLink(this, task)

    fun <O2> flatMap(f: (O) -> Future<O2>) = this.andThen(AsyncTask(f))

    fun <O2> map(f: (O) -> O2) = this.andThen(SyncTask(f))

    fun <O2> sqlUpdate(transaction: SQLTransaction<O, O2>, executor: SQLTransactionExecutor) =
            this.andThen(UnpreparedSQLUpdateTask(transaction).prepare(executor))

    fun <O2> sqlQuery(transaction: SQLTransaction<O, O2>, executor: SQLTransactionExecutor) =
            this.andThen(UnpreparedSQLQueryTask(transaction).prepare(executor))

    fun <O2> sqlExec(transaction: SQLTransaction<O, O2>, executor: SQLTransactionExecutor): Task<I, O2> =
            this.andThen(UnpreparedSQLExecTask(transaction).prepare(executor))

    fun <O2> vertx(vertxAction: (O, Vertx) -> Future<O2>, vertx: Vertx): Task<I, O2> =
            this.andThen(UnpreparedVertxTask(vertxAction).prepare(vertx))
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

interface UnpreparedTask<I, O, P> {
    fun prepare(p: P): Task<I, O>
}

data class UnpreparedVertxTask<I, O>(val vertxAction: (I, Vertx) -> Future<O>): UnpreparedTask<I, O, Vertx> {
    override fun prepare(p: Vertx): Task<I, O> {
        return AsyncTask( { vertxAction(it, p) })
    }
}

data class UnpreparedSQLQueryTask<I, O>(val transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, SQLTransactionExecutor>{
    override fun prepare(p: SQLTransactionExecutor): Task<I, O> {
        return AsyncTask( { p.query(it, transaction) })
    }
}

data class UnpreparedSQLUpdateTask<I, O>(val transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, SQLTransactionExecutor>{
    override fun prepare(p: SQLTransactionExecutor): Task<I, O> {
        return AsyncTask( { p.update(it, transaction) })
    }
}

data class UnpreparedSQLExecTask<I, O>(val transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, SQLTransactionExecutor> {
    override fun prepare(p: SQLTransactionExecutor): Task<I, O> {
        return AsyncTask({ p.execute(it, transaction)})
    }
}

