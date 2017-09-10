package dev.yn.playground.sql

import io.vertx.core.Future
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection

class SQLTransactionExecutor(val client: SQLClient) {
    fun <I, J, O> query(i: I, transaction: SQLTransaction<I, J, O>): Future<O> {
        val future: Future<SQLConnection> = Future.future()
        client.getConnection(future.completer())
        return future.compose { connection ->
            transaction.run(i, connection)
        }
    }
    fun execute(i: Unit, transaction: SQLTransaction<Unit, Unit, Unit>): Future<Unit> {
        return query(i, transaction)
    }

    fun <I, J, O> update(i: I, transaction: SQLTransaction<I, J, O>): Future<O> {
        val future: Future<SQLConnection> = Future.future()
        client.getConnection(future.completer())
        return future
                .compose { connection ->
                    val confFuture: Future<Void> = Future.future()
                    connection.setAutoCommit(false, confFuture.completer())
                    confFuture.map { connection }
                }.compose { connection ->
            transaction.run(i, connection).compose { result ->
                val commitFuture: Future<Void> = Future.future()
                connection.commit(commitFuture.completer())
                commitFuture.map { result }
            }.recover { error ->
                val rollbackFuture: Future<Void> = Future.future()
                connection.rollback(rollbackFuture.completer())
                rollbackFuture.compose { Future.failedFuture<O>(error) }
            }
        }
    }
}
