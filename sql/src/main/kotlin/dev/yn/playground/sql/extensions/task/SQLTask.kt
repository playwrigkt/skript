package dev.yn.playground.sql.extensions.task

import dev.yn.playground.sql.SQLTransaction
import dev.yn.playground.sql.SQLTransactionExecutor
import dev.yn.playground.task.AsyncTask
import dev.yn.playground.task.PreparedTask
import dev.yn.playground.task.Task
import dev.yn.playground.task.UnpreparedTask

object SQLTask {
    fun <I, O, P: SQLTransactionExecutorProvider> sqlTransaction(transaction: SQLTransaction<I, O>, provider: P) =
            UnpreparedTransactionalSQLTask<I, O, P>(transaction).prepare(provider)

    fun <I, O, P: SQLTransactionExecutorProvider> sql(transaction: SQLTransaction<I, O>, provider: P) =
            UnpreparedSQLTask<I, O, P>(transaction).prepare(provider)

    fun <I, O, P: SQLTransactionExecutorProvider> unpreparedSql(transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, P> =
            UnpreparedSQLTask<I, O, P>(transaction)

    fun <I, O, P: SQLTransactionExecutorProvider> unpreparedTransactionalSql(transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, P> =
            UnpreparedTransactionalSQLTask<I, O, P>(transaction)

}

fun <I, O, O2, P: SQLTransactionExecutorProvider> Task<I, O>.sqlTransaction(transaction: SQLTransaction<O, O2>, provider: P) =
        this.andThen(UnpreparedTransactionalSQLTask<O, O2, P>(transaction).prepare(provider))

fun <I, O, O2, P: SQLTransactionExecutorProvider> Task<I, O>.sql(transaction: SQLTransaction<O, O2>, provider: P) =
        this.andThen(UnpreparedSQLTask<O, O2, P>(transaction).prepare(provider))

fun <I, O, O2, P: SQLTransactionExecutorProvider> UnpreparedTask<I, O, P>.unpreparedSql(transaction: SQLTransaction<O, O2>): UnpreparedTask<I, O2, P> =
        this.andThen(UnpreparedSQLTask(transaction))

fun <I, O, O2, P: SQLTransactionExecutorProvider> UnpreparedTask<I, O, P>.unpreparedSqlTransaction(transaction: SQLTransaction<O, O2>): UnpreparedTask<I, O2, P> =
        this.andThen(UnpreparedTransactionalSQLTask(transaction))

/**
 * Interface for providing a SQLTransactionExecutor
 */
interface SQLTransactionExecutorProvider {
    fun provideSQLTransactionExecutor(): SQLTransactionExecutor
}

/**
 * A SQL Task that neeeds to be prepared
 */
data class UnpreparedSQLTask<I, O, P: SQLTransactionExecutorProvider>(val transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, P> {
    override fun prepare(p: P): Task<I, O> {
        return PreparedTask( { i, connection -> connection.execute(i, transaction) }, p.provideSQLTransactionExecutor())
    }
}

/**
 * A SQL Task that needs to be prepared
 * Will be run within a transacteion and rollback if there is a failure (exception)
 */
data class UnpreparedTransactionalSQLTask<I, O, P: SQLTransactionExecutorProvider>(val transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, P>{
    override fun prepare(p: P): Task<I, O> {
        return AsyncTask( { p.provideSQLTransactionExecutor().executeTransactionally(it, transaction) })
    }
}