package dev.yn.playground.sql

import io.vertx.core.Future
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection

/**
 * Executes SQl transactions asynhronously and manages commit/rollback based on the results.
 *
 * TODO: Can this be completely replaced by Task?
 */
class SQLTransactionExecutor(val client: SQLClient) {
    companion object {
        /**
         * Execute the unpreparedSql transaction, and commit on complete or roll back if there is an error
         */
        fun <I, O> executeTransactionally(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, O>): (I) -> Future<O> {
            return { executor.executeTransactionally(it, transaction) }
        }

        /**
         * Execute the unpreparedSql transaction, and commit on complete or roll back if there is an error
         */
        fun <I, O> executeTransactionally(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, O>, i: I): Future<O> {
            return executor.executeTransactionally(i, transaction)
        }

        /**
         * Execute the unpreparedSql transaction, do not use this method with any transctions that alter the database as it will
         * neither commit nor rollback
         */
        fun <I, O> execute(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, O>): (I) -> Future<O> {
            return { executor.execute(it, transaction) }
        }

        /**
         * Execute the unpreparedSql transaction, do not use this method with any transctions that alter the database as it will
         * neither commit nor rollback
         */
        fun <I, O> execute(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, O>, i: I): Future<O> {
            return executor.execute(i, transaction)
        }
    }

    /**
     * Execute the unpreparedSql transaction, do not use this method with any transctions that alter the database as it will
     * neither commit nor rollback
     */
    fun <I, O> execute(i: I, transaction: SQLTransaction<I, O>): Future<O> {
        val future: Future<SQLConnection> = Future.future()
        client.getConnection(future.completer())
        return future.compose { connection ->
            transaction.run(i, connection)
        }
    }

    /**
     *  Execute a unpreparedSql transaction on the input I, and commit on complete or roll back if there is an error
     */
    fun <I, O> executeTransactionally(i: I, transaction: SQLTransaction<I, O>): Future<O> {
        val future: Future<SQLConnection> = Future.future()
        client.getConnection(future.completer())
        return future
                .compose { connection ->
                    val confFuture: Future<Void> = Future.future()
                    connection.setAutoCommit(false, confFuture.completer())
                    confFuture.map { connection } }
                .compose { connection ->
                    transaction.run(i, connection)
                            .compose { result ->
                                val commitFuture: Future<Void> = Future.future()
                                connection.commit(commitFuture.completer())
                                commitFuture.map { result } }
                            .recover { error ->
                                val rollbackFuture: Future<Void> = Future.future()
                                connection.rollback(rollbackFuture.completer())
                                rollbackFuture.compose { Future.failedFuture<O>(error) } }
        }
    }
}
