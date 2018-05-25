package playwrigkt.skript.sql

import arrow.core.Try
import java.time.Instant

sealed class SqlResult {
    data class Query(val result: Iterator<SqlRow>): SqlResult()
    data class Update(val count: Int): SqlResult()
    object Void: SqlResult()
}

interface SqlRow {
    //TODO all data types
    fun getString(key: String): String
    fun getBoolean(key: String): Boolean
    fun getLong(key: String): Long
    fun getInt(key: String): Int
    fun getInstant(key: String): Instant
}

sealed class SqlCommand {
    data class Query(val statement: SqlStatement) : SqlCommand()
    data class Update(val statement: SqlStatement) : SqlCommand()
    data class Exec(val statement: SqlStatement) : SqlCommand()
}

sealed class SqlStatement {
    data class Parameterized(val query: String, val params: List<Any>): SqlStatement()
    data class Simple(val query: String): SqlStatement()
}

typealias SqlUpdateMapping<I, O> = SqlMapping<I, O, SqlCommand.Update, SqlResult.Update>
typealias SqlQueryMapping<I, O> = SqlMapping<I, O, SqlCommand.Query, SqlResult.Query>
typealias SqlExecMapping<I, O> = SqlMapping<I, O, SqlCommand.Exec, SqlResult.Void>

interface SqlMapping<I, O, C: SqlCommand, R: SqlResult> {
    fun toSql(i: I): C
    fun mapResult(i: I, rs: R): Try<O>

    companion object {
        fun <I, O, C: SqlCommand, R: SqlResult> new(toSql: (I) -> C, mapResult: (I, R) -> Try<O>): SqlMapping<I, O, C, R> =
                object: SqlMapping<I, O, C, R> {
                    override fun toSql(i: I): C = toSql(i)
                    override fun mapResult(i: I, rs: R): Try<O> = mapResult(i, rs)
                }

        fun <I, O> query(toSql: (I) -> SqlCommand.Query, mapResult: (I, SqlResult.Query) -> Try<O>): SqlMapping<I, O, SqlCommand.Query, SqlResult.Query> =
                object: SqlMapping<I, O, SqlCommand.Query, SqlResult.Query> {
                    override fun toSql(i: I): SqlCommand.Query = toSql(i)
                    override fun mapResult(i: I, rs: SqlResult.Query): Try<O> = mapResult(i, rs)
                }

        fun <I, O> update(toSql: (I) -> SqlCommand.Update, mapResult: (I, SqlResult.Update) -> Try<O>): SqlMapping<I, O, SqlCommand.Update, SqlResult.Update> =
                object: SqlMapping<I, O, SqlCommand.Update, SqlResult.Update> {
                    override fun toSql(i: I): SqlCommand.Update = toSql(i)
                    override fun mapResult(i: I, rs: SqlResult.Update): Try<O> = mapResult(i, rs)
                }

        fun <I, O> exec(toSql: (I) -> SqlCommand.Exec, mapResult: (I, SqlResult.Void) -> Try<O>): SqlMapping<I, O, SqlCommand.Exec, SqlResult.Void> =
                object: SqlMapping<I, O, SqlCommand.Exec, SqlResult.Void> {
                    override fun toSql(i: I): SqlCommand.Exec = toSql(i)
                    override fun mapResult(i: I, rs: SqlResult.Void): Try<O> = mapResult(i, rs)
                }

        fun exec(command: String): SqlMapping<Unit, Unit, SqlCommand.Exec, SqlResult.Void> = exec(
                { SqlCommand.Exec(SqlStatement.Simple(command)) },
                { unit, _ -> Try.Success(unit) })

    }
}

