package dev.yn.playground.sql.task

import dev.yn.playground.sql.SQLAction
import dev.yn.playground.sql.UnpreparedSQLAction
import dev.yn.playground.task.Task
import io.vertx.core.Future
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection

/**
 * Handles the execution of a SQLActionChain without a transaction
 */
internal data class SQLTask<I, O>(val actionChain: SQLAction<I, O>, val sqlClient: SQLClient): Task<I, O> {
    companion object {
        fun <I, O, P: SQLClientProvider> sqlTransaction(action: UnpreparedSQLAction<I, O, P>, provider: P) =
                UnpreparedTransactionalSQLTask<I, O, P>(action).prepare(provider)

        fun <I, O, P: SQLClientProvider> sql(action: UnpreparedSQLAction<I, O, P>, provider: P) =
                UnpreparedSQLTask<I, O, P>(action).prepare(provider)

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

        fun <T> commit(connection: SQLConnection): (T) -> Future<T> = { thing ->
            val future = Future.future<Void>()
            connection.commit(future.completer())
            future.map { thing }
        }

        fun <T> rollback(connection: SQLConnection): (Throwable) -> Future<T> = { error ->
            val future = Future.future<Void>()
            connection.rollback(future.completer())
            future.compose<T> { Future.failedFuture(error) }
        }

        fun setAutoCommit(autoCommit: Boolean, connection: SQLConnection): Future<Void> {
            val confFuture: Future<Void> = Future.future()
            connection.setAutoCommit(autoCommit, confFuture.completer())
            return confFuture
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
                    SQLTask.setAutoCommit(true, connection)
                            .compose { actionChain.run(i, connection) }
                            .compose(SQLTask.close(connection))
                            .recover(SQLTask.closeOnFailure(connection))
                }
    }
}

/**
 * Handle the transactional execution of a SQLActionChain
 */
internal data class TransactionalSQLTask<I, O>(val action: SQLAction<I, O>, val sqlClient: SQLClient): Task<I, O> {

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
                    SQLTask.setAutoCommit(false, connection)
                            .compose {
                                action.run(i, connection)
                                    .compose(SQLTask.commit(connection))
                                    .recover(SQLTask.rollback(connection))
                            }
                            .compose(SQLTask.close(connection))
                            .recover(SQLTask.closeOnFailure(connection))
                }
    }
}

