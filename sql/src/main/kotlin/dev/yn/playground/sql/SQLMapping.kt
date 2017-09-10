package dev.yn.playground.sql

import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.UpdateResult
import org.funktionale.tries.Try

interface SQLMapping<I, O, RS> {
    fun toSql(i: I): SQLStatement
    fun mapResult(i: I, rs: RS): O
}
interface UpdateSQLMapping<I, O>: SQLMapping<I, Try<O>, UpdateResult>
interface QuerySQLMapping<I, O>: SQLMapping<I, Try<O>, ResultSet>