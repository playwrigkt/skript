package dev.yn.playground.sql

import io.vertx.core.Future
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection

/**
 * Executes SQl transactions asynhronously and manages commit/rollback based on the results.
 */
class SQLTransactionExecutor(val client: SQLClient) {
    companion object {
        /**
         * Execute the sql transaction, and commit on complete or roll back if there is an error
         */
        fun <I, J, O> update(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, J, O>): (I) -> Future<O> {
            return { executor.update(it, transaction) }
        }

        /**
         * Execute the sql transaction, and commit on complete or roll back if there is an error
         */
        fun <I, J, O> update(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, J, O>, i: I): Future<O> {
            return executor.update(i, transaction)
        }

        /**
         * Execute the sql transaction, do not use this method with any transctions that alter the database as it will
         * neither commit nor rollback
         */
        fun <I, J, O> query(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, J, O>): (I) -> Future<O> {
            return { executor.query(it, transaction) }
        }

        /**
         * Execute the sql transaction, do not use this method with any transctions that alter the database as it will
         * neither commit nor rollback
         */
        fun <I, J, O> query(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, J, O>, i: I): Future<O> {
            return executor.query(i, transaction)
        }

        /**
         * Execute the sql transaction, and commit on complete or roll back if there is an error
         */
        fun execute(executor: SQLTransactionExecutor, transaction: SQLTransaction<Unit, Unit, Unit>): Future<Unit> {
            return executor.execute(transaction)
        }
    }

    /**
     * Execute the sql transaction, do not use this method with any transctions that alter the database as it will
     * neither commit nor rollback
     */
    fun <I, J, O> query(i: I, transaction: SQLTransaction<I, J, O>): Future<O> {
        val future: Future<SQLConnection> = Future.future()
        client.getConnection(future.completer())
        return future.compose { connection ->
            transaction.run(i, connection)
        }
    }

    /**
     *  Execute a sql transaction with no input, and commit on complete or roll back if there is an error
     */
    fun execute(transaction: SQLTransaction<Unit, Unit, Unit>): Future<Unit> {
        val future: Future<SQLConnection> = Future.future()
        client.getConnection(future.completer())
        return future
                .compose { connection ->
                    val confFuture: Future<Void> = Future.future()
                    connection.setAutoCommit(false, confFuture.completer())
                    confFuture.map { connection } }
                .compose { connection ->
                    transaction.run(Unit, connection)
                            .compose { result ->
                                val commitFuture: Future<Void> = Future.future()
                                connection.commit(commitFuture.completer())
                                commitFuture.map { result } }
                            .recover { error ->
                                val rollbackFuture: Future<Void> = Future.future()
                                connection.rollback(rollbackFuture.completer())
                                rollbackFuture.compose { Future.failedFuture<Unit>(error) } }
                }
    }

    /**
     *  Execute a sql transaction on the input I, and commit on complete or roll back if there is an error
     */
    fun <I, J, O> update(i: I, transaction: SQLTransaction<I, J, O>): Future<O> {
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
