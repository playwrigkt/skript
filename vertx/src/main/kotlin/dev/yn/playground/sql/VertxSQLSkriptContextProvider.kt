package dev.yn.playground.sql

import dev.yn.playground.context.SQLSkriptContextProvider
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.result.VertxResult
import io.vertx.core.Future
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection

data class VertxSQLSkriptContextProvider(val sqlClient: SQLClient): SQLSkriptContextProvider<VertxSQLExecutor> {
    override fun getConnection(): AsyncResult<VertxSQLExecutor> {
        val future = Future.future<SQLConnection>()
        sqlClient.getConnection(future.completer())
        return VertxResult(future.map { VertxSQLExecutor(it) })
    }
}