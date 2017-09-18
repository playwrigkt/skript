package dev.yn.playground.sql.task

import dev.yn.playground.sql.UnpreparedSQLActionChain
import dev.yn.playground.task.Task
import dev.yn.playground.task.UnpreparedTask
import io.vertx.ext.sql.SQLClient

fun <I, O, O2, P: SQLClientProvider> UnpreparedTask<I, O, P>.sql(actionChain: UnpreparedSQLActionChain<O, O2, P>): UnpreparedTask<I, O2, P> =
        this.andThen(UnpreparedSQLTask(actionChain))

fun <I, O, O2, P: SQLClientProvider> UnpreparedTask<I, O, P>.sqlTransaction(actionChain: UnpreparedSQLActionChain<O, O2, P>): UnpreparedTask<I, O2, P> =
        this.andThen(UnpreparedTransactionalSQLTask(actionChain))

/**
 * Interface for providing a SQLClient to tasks
 */
interface SQLClientProvider {
    fun provideSQLClient(): SQLClient
}

/**
 * A SQL Task that neeeds to be prepared
 * Will not wrap the actionChain in a transaction
 */
data class UnpreparedSQLTask<I, O, P: SQLClientProvider>(val actionChain: UnpreparedSQLActionChain<I, O, P>): UnpreparedTask<I, O, P> {
    companion object {
        fun <I, O, P: SQLClientProvider> chain(actionChain: UnpreparedSQLActionChain<I, O, P>): UnpreparedTask<I, O, P> =
                UnpreparedSQLTask<I, O, P>(actionChain)
    }
    override fun prepare(p: P): Task<I, O> {
        return SQLTask(actionChain.prepare(p), p.provideSQLClient())
    }
}

/**
 * A SQL Task that needs to be prepared
 * Will be run within a actionChain and rollback if there is a failure (exception)
 */
data class UnpreparedTransactionalSQLTask<I, O, P: SQLClientProvider>(val actionChain: UnpreparedSQLActionChain<I, O, P>): UnpreparedTask<I, O, P> {
    companion object {
        fun <I, O, P: SQLClientProvider> chain(actionChain: UnpreparedSQLActionChain<I, O, P>): UnpreparedTask<I, O, P> =
                UnpreparedTransactionalSQLTask<I, O, P>(actionChain)
    }
    override fun prepare(p: P): Task<I, O> {
        return TransactionalSQLTask(actionChain.prepare(p), p.provideSQLClient())
    }
}