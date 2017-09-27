package dev.yn.playground.sql.task

import dev.yn.playground.sql.SQLActionChain
import dev.yn.playground.sql.UnpreparedSQLActionChain
import dev.yn.playground.task.Task
import io.vertx.core.Future
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection

/**
 * Handles the execution of a SQLActionChain without a transaction
 */
internal data class SQLTask<I, O>(val actionChain: SQLActionChain<I, O>, val sqlClient: SQLClient): Task<I, O> {
    companion object {
        fun <I, O, P: SQLClientProvider> sqlTransaction(actionChain: UnpreparedSQLActionChain<I, O, P>, provider: P) =
                UnpreparedTransactionalSQLTask<I, O, P>(actionChain).prepare(provider)

        fun <I, O, P: SQLClientProvider> sql(actionChain: UnpreparedSQLActionChain<I, O, P>, provider: P) =
                UnpreparedSQLTask<I, O, P>(actionChain).prepare(provider)

        fun <T> close(connection: SQLConnection): (T) -> Future<T> = { thing ->
            val future = Future.future<Void>()
            connection.close(future.completer())
            future.map { thing }
        }

        fun <T> closeOnFailure(connection: SQLConnection): (Throwable) -> Future<T> = { error ->
            val future = Future.future<Void>()
            connection.close(future.completer())
            future.compose<T> { Future.failedFuture(error) }
        }
    }

    /**
     * Executes a sql head chain on a single connection with autocommit set to true
     */
    override fun run(i: I): Future<O> {
        val future: Future<SQLConnection> = Future.future()
        sqlClient.getConnection(future.completer())
        return future
                .compose { connection ->
                    val confFuture: Future<Void> = Future.future()
                    connection.setAutoCommit(true, confFuture.completer())
                    confFuture
                            .map { connection }
                            .recover(SQLTask.closeOnFailure(connection)) }
                .compose { connection ->
                    actionChain.run(i, connection)
                            .compose(SQLTask.close(connection))
                            .recover(SQLTask.closeOnFailure(connection))
                }
    }
}

/**
 * Handle the transactional execution of a SQLActionChain
 */
internal data class TransactionalSQLTask<I, O>(val actionChain: SQLActionChain<I, O>, val sqlClient: SQLClient): Task<I, O> {

    /**
     * Executes a sql head chain on a single connection with autocommit set to false
     *
     * Commits if the future is successful
     * Rolls back if the future fails
     */
    override fun run(i: I): Future<O> {
        val future: Future<SQLConnection> = Future.future()
        sqlClient.getConnection(future.completer())
        return future
                .compose { connection ->
                    val confFuture: Future<Void> = Future.future()
                    connection.setAutoCommit(false, confFuture.completer())
                    confFuture
                            .map { connection }
                            .recover(SQLTask.closeOnFailure(connection)) }
                .compose { connection ->
                    actionChain.run(i, connection)
                            .compose { result ->
                                val commitFuture: Future<Void> = Future.future()
                                connection.commit(commitFuture.completer())
                                commitFuture.map { result }
                            }
                            .recover { error ->
                                val rollbackFuture: Future<Void> = Future.future()
                                connection.rollback(rollbackFuture.completer())
                                rollbackFuture
                                        .compose { Future.failedFuture<O>(error) } }
                            .compose(SQLTask.close(connection))
                            .recover(SQLTask.closeOnFailure(connection))
                }
    }
}

