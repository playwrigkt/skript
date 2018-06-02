package playwrigkt.skript.performer

import arrow.core.Try
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.coroutine.runTryAsync
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.sql.*
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class CoroutineJdbcSqlPerformer(val connection: Connection): playwrigkt.skript.performer.SqlPerformer() {
    override fun query(query: SqlCommand.Query): AsyncResult<SqlResult.Query> {
        val statement = query.statement
        return runAsync {
            when (statement) {
                is SqlStatement.Simple ->
                    connection.createStatement().executeQuery(statement.query)
                is SqlStatement.Parameterized -> {
                    val preparedStatement = connection.prepareStatement(statement.query)
                    statement.params.forEachIndexed { idx, value -> preparedStatement.setObject(idx + 1, value) }
                    preparedStatement.executeQuery()
                } } }
                .map { SqlResult.Query(playwrigkt.skript.performer.CoroutineJdbcSqlPerformer.JdbcIterator(it)) }
    }

    override fun update(update: SqlCommand.Update): AsyncResult<SqlResult.Update> {
        val statement = update.statement
        return runAsync {
            when(statement) {
                is SqlStatement.Simple -> connection.createStatement().executeUpdate(statement.query)
                is SqlStatement.Parameterized -> {
                    val preparedStatement = connection.prepareStatement(statement.query)
                    statement.params.forEachIndexed { idx, value -> preparedStatement.setObject(idx + 1, value) }
                    preparedStatement.executeUpdate()
                } } }
                .map { SqlResult.Update(it) }
    }

    override fun exec(exec: SqlCommand.Exec): AsyncResult<SqlResult.Void> {
        val statement = exec.statement
        return runAsync { when(statement) {
                is SqlStatement.Simple ->
                    connection.createStatement().execute(statement.query)
                is SqlStatement.Parameterized -> {
                    val preparedStatement = connection.prepareStatement(statement.query)
                    statement.params.forEachIndexed { idx, value -> preparedStatement.setObject(idx + 1, value) }
                    preparedStatement.execute()
                } } }
                .map { SqlResult.Void }
    }

    override fun <T> close(): (T) -> AsyncResult<T> = { t ->
        runAsync {
            connection.close()
            t
        }
    }

    override fun <T> closeOnFailure(): (Throwable) -> AsyncResult<T> = { f ->
        runTryAsync {
            Try { connection.close() }
                    .flatMap { Try.Failure<T>(f) }
        }
    }

    override fun <T> commit(): (T) -> AsyncResult<T> = { t ->
        runAsync {
            connection.commit()
            t
        }
    }

    override fun <T> rollback(): (Throwable) -> AsyncResult<T> = { f ->
        runTryAsync {
            Try { connection.rollback() }
                    .flatMap { Try.Failure<T>(f) }
        }
    }

    override fun setAutoCommit(autoCommit: Boolean): AsyncResult<Unit> {
        return runAsync {
            connection.setAutoCommit(autoCommit)
        }
    }


    private class JdbcSqlRow(val row: Map<String, Any>): SqlRow {
        companion object {
            fun of(row: ResultSet): playwrigkt.skript.performer.CoroutineJdbcSqlPerformer.JdbcSqlRow {
                return playwrigkt.skript.performer.CoroutineJdbcSqlPerformer.JdbcSqlRow(
                        (1..row.metaData.columnCount)
                                .map {
                                    //TODO all data types
                                    row.metaData.getColumnName(it) to when (row.metaData.getColumnType(it)) {
                                        Types.BOOLEAN -> row.getBoolean(it)
                                        Types.VARCHAR, Types.NVARCHAR, Types.LONGVARCHAR, Types.LONGNVARCHAR, Types.NCHAR, Types.CHAR -> row.getString(it)
                                        Types.INTEGER -> row.getInt(it)
                                        Types.BIGINT -> row.getLong(it)
                                        Types.TIME -> row.getTime(it)
                                        Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> row.getTimestamp(it)
                                        Types.DATE -> row.getDate(it)
                                        else -> row.getObject(it)
                                    }
                                }
                                .toMap())
            }
        }

        override fun getString(key: String): String {
            val result = row.get(key)
            when(result) {
                is String -> return result
                else -> throw SqlError.SqlRowError(result, "Value was not a string")
            }
        }

        override fun getBoolean(key: String): Boolean {
            val result = row.get(key)
            when(result) {
                is Boolean -> return result
                else -> throw SqlError.SqlRowError(result, "Value was not a boolean")
            }
        }

        override fun getLong(key: String): Long {
            val result = row.get(key)
            return when(result) {
                is Long -> result
                else -> throw SqlError.SqlRowError(result, "Value was not a long")
            }
        }

        override fun getInt(key: String): Int {
            val result = row.get(key)
            when(result) {
                is Int -> return result
                else -> throw SqlError.SqlRowError(result, "Value was not an int")
            }
        }

        override fun getInstant(key: String): Instant {
            val result = row.get(key)
            when(result) {
                is Timestamp -> return result.toInstant()
                else -> throw SqlError.SqlRowError(result, "Value was not an instant")
            }
        }
    }

    private class JdbcIterator(val rs: ResultSet): Iterator<playwrigkt.skript.performer.CoroutineJdbcSqlPerformer.JdbcSqlRow> {
        val queue = LinkedBlockingQueue<playwrigkt.skript.performer.CoroutineJdbcSqlPerformer.JdbcSqlRow>()
        override fun hasNext(): Boolean {
            if(queue.isNotEmpty()) {
                return true
            } else if(rs.next()) {
                queue.offer(playwrigkt.skript.performer.CoroutineJdbcSqlPerformer.JdbcSqlRow.Companion.of(rs))
                return true
            } else {
                return false
            }
        }

        override fun next(): playwrigkt.skript.performer.CoroutineJdbcSqlPerformer.JdbcSqlRow {
            return Optional.of(hasNext())
                    .filter { it }
                    .flatMap { Optional.of(queue.poll()) }
                    .orElseThrow { SqlError.RowIteratorEmpty }
        }

    }
}