package dev.yn.playground.sql

import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.UpdateResult
import org.funktionale.tries.Try

/**
 * Interface for mapping to and from SQL Queries.  Allows for straightforward mapping that doesn't complicate the
 * creation of SQL
 */
interface SQLMapping<I, O, RS> {
    fun toSql(i: I): SQLStatement
    fun mapResult(i: I, rs: RS): O
}

interface UpdateSQLMapping<I, O>: SQLMapping<I, Try<O>, UpdateResult> {
    companion object {
        fun <I, O> create(toSql: (I) -> SQLStatement, mapResult: (I, UpdateResult) -> Try<O>): UpdateSQLMapping<I, O> =
                object: UpdateSQLMapping<I, O> {
                    override fun toSql(i: I): SQLStatement = toSql(i)
                    override fun mapResult(i: I, rs: UpdateResult): Try<O> = mapResult(i, rs)
                }
    }
}
interface QuerySQLMapping<I, O>: SQLMapping<I, Try<O>, ResultSet> {
    companion object {
        fun <I, O> create(toSql: (I) -> SQLStatement, mapResult: (I, ResultSet) -> Try<O>): QuerySQLMapping<I, O> =
                object: QuerySQLMapping<I, O> {
                    override fun toSql(i: I): SQLStatement =toSql(i)
                    override fun mapResult(i: I, rs: ResultSet): Try<O> = mapResult(i, rs)
                }
    }
}

