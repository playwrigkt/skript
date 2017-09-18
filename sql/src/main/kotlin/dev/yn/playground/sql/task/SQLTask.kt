package dev.yn.playground.sql.task

import dev.yn.playground.sql.SQLActionChain
import dev.yn.playground.sql.UnpreparedSQLActionChain
import dev.yn.playground.task.Task
import io.vertx.core.Future
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection

internal data class SQLTask<I, O>(val actionChain: SQLActionChain<I, O>, val sqlClient: SQLClient): Task<I, O> {
    companion object {
        fun <I, O, P: SQLClientProvider> sqlTransaction(actionChain: UnpreparedSQLActionChain<I, O, P>, provider: P) =
                UnpreparedTransactionalSQLTask<I, O, P>(actionChain).prepare(provider)

        fun <I, O, P: SQLClientProvider> sql(actionChain: UnpreparedSQLActionChain<I, O, P>, provider: P) =
                UnpreparedSQLTask<I, O, P>(actionChain).prepare(provider)
    }
    override fun run(i: I): Future<O> {
        val future: Future<SQLConnection> = Future.future()
        sqlClient.getConnection(future.completer())
        return future
                .compose { connection ->
                    val confFuture: Future<Void> = Future.future()
                    connection.setAutoCommit(true, confFuture.completer())
                    confFuture.map { connection } }
                .compose { connection ->
                    actionChain.run(i, connection)
                }
    }
}

internal data class TransactionalSQLTask<I, O>(val actionChain: SQLActionChain<I, O>, val sqlClient: SQLClient): Task<I, O> {
    override fun run(i: I): Future<O> {
        val future: Future<SQLConnection> = Future.future()
        sqlClient.getConnection(future.completer())
        return future
                .compose { connection ->
                    val confFuture: Future<Void> = Future.future()
                    connection.setAutoCommit(false, confFuture.completer())
                    confFuture.map { connection } }
                .compose { connection ->
                    actionChain.run(i, connection)
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

