package dev.yn.playground.sql.extensions.task

import dev.yn.playground.sql.SQLTransaction
import dev.yn.playground.sql.SQLTransactionExecutor
import dev.yn.playground.task.AsyncTask
import dev.yn.playground.task.Task
import dev.yn.playground.task.UnpreparedTask

object SQLTask {
    fun <I, O, P: SQLTransactionExecutorProvider> sqlUpdate(transaction: SQLTransaction<I, O>, provider: P) =
            UnpreparedSQLUpdateTask<I, O, P>(transaction).prepare(provider)

    fun <I, O, P: SQLTransactionExecutorProvider> sqlQuery(transaction: SQLTransaction<I, O>, provider: P) =
            UnpreparedSQLQueryTask<I, O, P>(transaction).prepare(provider)

    fun <I, O, P: SQLTransactionExecutorProvider> sqlExec(transaction: SQLTransaction<I, O>, provider: P): Task<I, O> =
            UnpreparedSQLExecTask<I, O, P>(transaction).prepare(provider)
}

fun <I, O, O2, P: SQLTransactionExecutorProvider> Task<I, O>.sqlUpdate(transaction: SQLTransaction<O, O2>, provider: P) =
        this.andThen(UnpreparedSQLUpdateTask<O, O2, P>(transaction).prepare(provider))

fun <I, O, O2, P: SQLTransactionExecutorProvider> Task<I, O>.sqlQuery(transaction: SQLTransaction<O, O2>, provider: P) =
        this.andThen(UnpreparedSQLQueryTask<O, O2, P>(transaction).prepare(provider))

fun <I, O, O2, P: SQLTransactionExecutorProvider> Task<I, O>.sqlExec(transaction: SQLTransaction<O, O2>, provider: P): Task<I, O2> =
        this.andThen(UnpreparedSQLExecTask<O, O2, P>(transaction).prepare(provider))

interface SQLTransactionExecutorProvider {
    fun provideSQLTransactionExecutor(): SQLTransactionExecutor
}

data class UnpreparedSQLQueryTask<I, O, in P: SQLTransactionExecutorProvider>(val transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, P> {
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
