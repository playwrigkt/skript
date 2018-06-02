package playwrigkt.skript.performer

import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.VertxResult
import playwrigkt.skript.sql.*
import java.time.Instant

class VertxSqlPerformer(val connection: SQLConnection): SqlPerformer() {
    override fun query(query: SqlCommand.Query): AsyncResult<SqlResult.Query> {
        val sqlFuture = Future.future<ResultSet>()
        val statement = query.statement
        when (statement) {
            is SqlStatement.Parameterized -> connection.queryWithParams(statement.query, JsonArray(statement.params), sqlFuture.completer())
            is SqlStatement.Simple -> connection.query(statement.query, sqlFuture.completer())
        }
        return VertxResult(sqlFuture)
                .map { SqlResult.Query(VertxRowIterator(it.rows.iterator())) }
                .recover { CompletableResult.failed(SqlError.OnCommand(query, it)) }
    }

    override fun update(update: SqlCommand.Update): AsyncResult<SqlResult.Update> {
        val sqlFuture = Future.future<UpdateResult>()
        val statement = update.statement
        when (statement) {
            is SqlStatement.Parameterized -> connection.updateWithParams(statement.query, JsonArray(statement.params), sqlFuture.completer())
            is SqlStatement.Simple -> connection.update(statement.query, sqlFuture.completer())
        }
        return VertxResult(sqlFuture)
                .map { SqlResult.Update(it.updated) }
                .recover { CompletableResult.failed(SqlError.OnCommand(update, it)) }
    }

    override fun exec(exec: SqlCommand.Exec): AsyncResult<SqlResult.Void> {
        val statement = exec.statement
        val query = when(statement) {
            is SqlStatement.Parameterized -> statement.query
            is SqlStatement.Simple -> statement.query
        }

        val sqlFuture = Future.future<Void>()
        connection.execute(query, sqlFuture.completer())
        return VertxResult(sqlFuture)
                .map { SqlResult.Void }
                .recover { CompletableResult.failed(SqlError.OnCommand(exec, it)) }
    }

    override fun <T> close(): (T) -> AsyncResult<T> = { thing ->
        val future = Future.future<Void>()
        connection.close(future.completer())
        VertxResult(future.map { thing })
    }

    override fun <T> closeOnFailure(): (Throwable) -> AsyncResult<T> = { error ->
        val future = Future.future<Void>()
        connection.close(future.completer())
        VertxResult(future.compose<T> { Future.failedFuture(error) })
    }

    override fun <T> commit(): (T) -> AsyncResult<T> = { thing ->
        val future = Future.future<Void>()
        connection.commit(future.completer())
        VertxResult(future.map { thing })
    }

    override fun <T> rollback(): (Throwable) -> AsyncResult<T> = { error ->
        val future = Future.future<Void>()
        connection.rollback(future.completer())
        VertxResult(future.compose<T> { Future.failedFuture(error) })
    }

    override fun setAutoCommit(autoCommit: Boolean): AsyncResult<Unit> {
        val confFuture: Future<Void> = Future.future()
        connection.setAutoCommit(autoCommit, confFuture.completer())
        return VertxResult(confFuture.map { Unit })
    }

    private class VertxSqlRow(val row: JsonObject): SqlRow {
        override fun getString(key: String): String {
            return row.getString(key)
        }

        override fun getBoolean(key: String): Boolean {
            return row.getBoolean(key)
        }

        override fun getLong(key: String): Long {
            return row.getLong(key)
        }

        override fun getInt(key: String): Int {
            return row.getInteger(key)
        }

        override fun getInstant(key: String): Instant {
            return row.getInstant(key)
        }
    }

    private class VertxRowIterator(val rs: Iterator<JsonObject>): Iterator<VertxSqlRow> {
        override fun hasNext(): Boolean {
            return rs.hasNext()
        }

        override fun next(): VertxSqlRow {
            return VertxSqlRow(rs.next())
        }
    }
}