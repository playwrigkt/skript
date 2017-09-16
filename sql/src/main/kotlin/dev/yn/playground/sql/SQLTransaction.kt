package dev.yn.playground.sql

import dev.yn.playground.util.TryUtil
import io.vertx.core.Future
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
import org.funktionale.tries.Try

/**
 * A re-usable SQL transaction.  Use SQLTransactinExecutor to handle transactions autmatically.
 *
 * A SQLTranaction is a chain of `SQLAction`
 *
 * I input Type
 * O output type
 */

sealed class SQLTransaction<I, O> {
    companion object {
        fun <I, O> new(action: SQLAction<I, O>): SQLTransaction<I, O> {
            return EndLink(action)
        }

        fun <I, O> query(toSql: (I) -> SQLStatement, mapResult: (I, ResultSet) -> Try<O>): SQLTransaction<I, O> =
                SQLTransaction.new(SQLAction.Query(toSql, mapResult))

        fun <I, O> query(mapping: QuerySQLMapping<I, O>): SQLTransaction<I, O> =
                query(mapping::toSql, mapping::mapResult)

        fun <I, O> update(toSql: (I) -> SQLStatement, mapResult: (I, UpdateResult) -> Try<O>): SQLTransaction<I, O> =
                SQLTransaction.new(SQLAction.Update(toSql, mapResult))

        fun <I, O> update(mapping: UpdateSQLMapping<I, O>): SQLTransaction<I, O> =
                update(mapping::toSql, mapping::mapResult)

        fun <I> exec(statment: String): SQLTransaction<I, I> =
                SQLTransaction.new(SQLAction.Exec(statment))

        fun <I> dropTable(tableName: String): SQLTransaction<I, I> =
                exec("DROP TABLE $tableName")

        fun <I> dropTableIfExists(tableName: String): SQLTransaction<I, I> =
                exec("DROP TABLE IF EXISTS $tableName")

        fun <I> deleteAll(tableName: (I) -> String): SQLTransaction<I, I> =
                update({ i -> SQLStatement.Simple("DELETE FROM ${tableName(i)}") }, { a, b -> Try.Success(a) })

        fun <I> deleteAll(tableName: String): SQLTransaction<I, I> =
                update({ SQLStatement.Simple("DELETE FROM $tableName") }, { a, b -> Try.Success(a) })
    }

    abstract fun <U> addAction(action: SQLAction<O, U>): SQLTransaction<I, U>
    abstract fun run(i: I, connection: SQLConnection): Future<O>

    data class EndLink<I, O>(val action: SQLAction<I, O>): SQLTransaction<I, O>() {
        override fun <U> addAction(action: SQLAction<O, U>): SQLTransaction<I, U> =
                ActionLink(this.action, EndLink(action))

        override fun run(i: I, connection: SQLConnection): Future<O> =
                action.run(i, connection)

        override fun toString(): String = "SQLTransaction.EndLink(action=$action)"
    }

    data class ActionLink<I, J, O>(val action: SQLAction<I, J>,
                                      val next: SQLTransaction<J, O>): SQLTransaction<I, O>() {
        override fun <U> addAction(action: SQLAction<O, U>): SQLTransaction<I, U> =
                ActionLink(this.action, next.addAction(action))

        override fun run(i: I, connection: SQLConnection): Future<O> =
                action.run(i, connection)
                        .compose { u -> next.run(u, connection) }

        override fun toString(): String = "SQLTransaction.ActionLink(action=$action,next=$next)"
    }

    fun <K> query(toSql: (O) -> SQLStatement, mapResult: (O, ResultSet) -> Try<K>): SQLTransaction<I, K> =
            addAction(SQLAction.Query(toSql, mapResult))

    fun <K> query(mapping: QuerySQLMapping<O, K>): SQLTransaction<I, K> =
            query(mapping::toSql, mapping::mapResult)

    fun <K> update(toSql: (O) -> SQLStatement, mapResult: (O, UpdateResult) -> Try<K>): SQLTransaction<I, K> =
            addAction(SQLAction.Update(toSql, mapResult))

    fun <K> update(mapping: UpdateSQLMapping<O, K>): SQLTransaction<I, K> =
            update(mapping::toSql, mapping::mapResult)

    fun exec(statment: String): SQLTransaction<I, O> =
            addAction(SQLAction.Exec(statment))

    fun dropTable(tableName: String): SQLTransaction<I, O> =
            exec("DROP TABLE $tableName")

    fun dropTableIfExists(tableName: String): SQLTransaction<I, O> =
            exec("DROP TABLE IF EXISTS $tableName")

    fun deleteAll(tableName: (O) -> String): SQLTransaction<I, O> =
            update({ o -> SQLStatement.Simple("DELETE FROM ${tableName(o)}") }, { a, b -> Try.Success(a) })

    fun <K> mapAsync(mapper: (O) -> Future<K>): SQLTransaction<I, K> =
            addAction(SQLAction.MapAsync(mapper))

    fun <K> map(mapper: (O) -> K): SQLTransaction<I, K> =
            addAction(SQLAction.Map(mapper))

    fun <K> flatMap(next: SQLTransaction<O, K>): SQLTransaction<I, K> =
            addAction(SQLAction.Nested(next))
}