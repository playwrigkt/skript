package dev.yn.playground.sql.task

import dev.yn.playground.sql.SQLTransaction
import dev.yn.playground.task.Task
import dev.yn.playground.task.UnpreparedTask
import io.vertx.core.Future
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection

fun <I, O, O2, P: SQLClientProvider> Task<I, O>.sqlTransaction(transaction: SQLTransaction<O, O2>, provider: P) =
        this.andThen(UnpreparedTransactionalSQLTask<O, O2, P>(transaction).prepare(provider))

fun <I, O, O2, P: SQLClientProvider> Task<I, O>.sql(transaction: SQLTransaction<O, O2>, provider: P) =
        this.andThen(UnpreparedSQLTask<O, O2, P>(transaction).prepare(provider))

data class SQLTask<I, O>(val transaction: SQLTransaction<I, O>, val sqlClient: SQLClient): Task<I, O> {
    companion object {
        fun <I, O, P: SQLClientProvider> sqlTransaction(transaction: SQLTransaction<I, O>, provider: P) =
                UnpreparedTransactionalSQLTask<I, O, P>(transaction).prepare(provider)

        fun <I, O, P: SQLClientProvider> sql(transaction: SQLTransaction<I, O>, provider: P) =
                UnpreparedSQLTask<I, O, P>(transaction).prepare(provider)

        fun <I, O, P: SQLClientProvider> unpreparedSql(transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, P> =
                UnpreparedSQLTask<I, O, P>(transaction)

        fun <I, O, P: SQLClientProvider> unpreparedTransactionalSql(transaction: SQLTransaction<I, O>): UnpreparedTask<I, O, P> =
                UnpreparedTransactionalSQLTask<I, O, P>(transaction)
    }
    override fun run(i: I): Future<O> {
        val future: Future<SQLConnection> = Future.future()
        sqlClient.getConnection(future.completer())
        return future.compose { connection ->
            transaction.run(i, connection)
        }
    }
}


data class TransactionalSQLTask<I, O>(val transaction: SQLTransaction<I, O>, val sqlClient: SQLClient): Task<I, O> {
    override fun run(i: I): Future<O> {
        val future: Future<SQLConnection> = Future.future()
        sqlClient.getConnection(future.completer())
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

