package dev.yn.playground.sql.task

import dev.yn.playground.sql.UnpreparedSQLAction
import dev.yn.playground.task.Task
import dev.yn.playground.task.UnpreparedTask
import io.vertx.ext.sql.SQLClient

fun <I, O, O2, P: SQLClientProvider> UnpreparedTask<I, O, P>.sql(action: UnpreparedSQLAction<O, O2, P>): UnpreparedTask<I, O2, P> =
        this.andThen(UnpreparedSQLTask(action))

fun <I, O, O2, P: SQLClientProvider> UnpreparedTask<I, O, P>.sqlTransaction(action: UnpreparedSQLAction<O, O2, P>): UnpreparedTask<I, O2, P> =
        this.andThen(UnpreparedTransactionalSQLTask(action))

/**
 * Interface for providing a SQLClient to tasks
 */
interface SQLClientProvider {
    fun provideSQLClient(): SQLClient
}

/**
 * A SQL Task that neeeds to be prepared
 * Will not wrap the action in a transaction
 */
data class UnpreparedSQLTask<I, O, P: SQLClientProvider>(val action: UnpreparedSQLAction<I, O, P>): UnpreparedTask<I, O, P> {
    companion object {
        fun <I, O, P: SQLClientProvider> create(action: UnpreparedSQLAction<I, O, P>): UnpreparedTask<I, O, P> =
                UnpreparedSQLTask<I, O, P>(action)
    }
    override fun prepare(p: P): Task<I, O> {
        return SQLTask(action.prepare(p), p.provideSQLClient())
    }
}

/**
 * A SQL Task that needs to be prepared
 * Will be run within a action and rollback if there is a failure (exception)
 */
data class UnpreparedTransactionalSQLTask<I, O, P: SQLClientProvider>(val action: UnpreparedSQLAction<I, O, P>): UnpreparedTask<I, O, P> {
    companion object {
        fun <I, O, P: SQLClientProvider> create(action: UnpreparedSQLAction<I, O, P>): UnpreparedTask<I, O, P> =
                UnpreparedTransactionalSQLTask<I, O, P>(action)
    }
    override fun prepare(p: P): Task<I, O> {
        return TransactionalSQLTask(action.prepare(p), p.provideSQLClient())
    }
}