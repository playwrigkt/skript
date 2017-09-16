package dev.yn.playground.sql.task

import dev.yn.playground.sql.SQLTransaction
import dev.yn.playground.task.Task
import dev.yn.playground.task.UnpreparedTask
import io.vertx.ext.sql.SQLClient

fun <I, O, O2, P: SQLClientProvider> UnpreparedTask<I, O, P>.unpreparedSql(transaction: SQLTransaction<O, O2>): UnpreparedTask<I, O2, P> =
        this.andThen(UnpreparedSQLTask(transaction))

fun <I, O, O2, P: SQLClientProvider> UnpreparedTask<I, O, P>.unpreparedSqlTransaction(transaction: SQLTransaction<O, O2>): UnpreparedTask<I, O2, P> =
        this.andThen(UnpreparedTransactionalSQLTask(transaction))


/**
 * Interface for providing a SQLClient to tasks
 */
interface SQLClientProvider {
    fun provideSQLClient(): SQLClient
}

/**
 * A SQL Task that neeeds to be prepared
 */
data class UnpreparedSQLTask<I, O, P: SQLClientProvider>(val transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, P> {
    override fun prepare(p: P): Task<I, O> {
        return SQLTask(transaction, p.provideSQLClient())
    }
}

/**
 * A SQL Task that needs to be prepared
 * Will be run within a transaction and rollback if there is a failure (exception)
 */
data class UnpreparedTransactionalSQLTask<I, O, P: SQLClientProvider>(val transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, P> {
    override fun prepare(p: P): Task<I, O> {
        return TransactionalSQLTask(transaction, p.provideSQLClient())
    }
}