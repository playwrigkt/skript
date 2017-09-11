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
 * J result of first transformation
 * O output type
 */
sealed class SQLTransaction<I, J, O> {
    companion object {
        fun <I, O> new(action: SQLAction<I, O>): SQLTransaction<I, O, O> {
            return EndLink(action)
        }

        fun <I, O> query(toSql: (I) -> SQLStatement, mapResult: (I, ResultSet) -> Try<O>): SQLTransaction<I, O, O> =
                SQLTransaction.new(SQLAction.Query(toSql, mapResult))

        fun <I, O> query(mapping: QuerySQLMapping<I, O>): SQLTransaction<I, O, O> =
                query(mapping::toSql, mapping::mapResult)

        fun <I, O> update(toSql: (I) -> SQLStatement, mapResult: (I, UpdateResult) -> Try<O>): SQLTransaction<I, O, O> =
                SQLTransaction.new(SQLAction.Update(toSql, mapResult))

        fun <I, O> update(mapping: UpdateSQLMapping<I, O>): SQLTransaction<I, O, O> =
                update(mapping::toSql, mapping::mapResult)

        fun exec(statment: String): SQLTransaction<Unit, Unit, Unit> =
                SQLTransaction.new(SQLAction.Exec(statment))

        fun dropTable(tableName: String): SQLTransaction<Unit, Unit, Unit> =
                exec("DROP TABLE $tableName")

        fun dropTableIfExists(tableName: String): SQLTransaction<Unit, Unit, Unit> =
                exec("DROP TABLE IF EXISTS $tableName")

        fun <I> deleteAll(tableName: (I) -> String): SQLTransaction<I, Unit, Unit> =
                update({ i -> SQLStatement.Simple("DELETE FROM ${tableName(i)}") }, { a, b -> TryUtil.unitSuccess })

        fun deleteAll(tableName: String): SQLTransaction<Unit, Unit, Unit> =
                update({ SQLStatement.Simple("DELETE FROM $tableName") }, { a, b -> TryUtil.unitSuccess })
    }

    abstract fun <U> addAction(action: SQLAction<O, U>): SQLTransaction<I, J, U>
    abstract fun run(i: I, connection: SQLConnection): Future<O>

    data class EndLink<I, O>(val action: SQLAction<I, O>): SQLTransaction<I, O, O>() {
        override fun <U> addAction(action: SQLAction<O, U>): SQLTransaction<I, O, U> =
                ActionLink(this.action, EndLink(action))

        override fun run(i: I, connection: SQLConnection): Future<O> =
                action.run(i, connection)

        override fun toString(): String = "SQLTransaction.EndLink(action=$action)"
    }

    data class ActionLink<I, J, K, O>(val action: SQLAction<I, J>,
                                      val next: SQLTransaction<J, K, O>): SQLTransaction<I, J, O>() {
        override fun <U> addAction(action: SQLAction<O, U>): SQLTransaction<I, J, U> =
                ActionLink(this.action, next.addAction(action))

        override fun run(i: I, connection: SQLConnection): Future<O> =
                action.run(i, connection)
                        .compose { u -> next.run(u, connection) }

        override fun toString(): String = "SQLTransaction.ActionLink(action=$action,next=$next)"
    }

    fun <K> query(toSql: (O) -> SQLStatement, mapResult: (O, ResultSet) -> Try<K>): SQLTransaction<I, J, K> =
            addAction(SQLAction.Query(toSql, mapResult))

    fun <K> query(mapping: QuerySQLMapping<O, K>): SQLTransaction<I, J, K> =
            query(mapping::toSql, mapping::mapResult)

    fun <K> update(toSql: (O) -> SQLStatement, mapResult: (O, UpdateResult) -> Try<K>): SQLTransaction<I, J, K> =
            addAction(SQLAction.Update(toSql, mapResult))

    fun <K> update(mapping: UpdateSQLMapping<O, K>): SQLTransaction<I, J, K> =
            update(mapping::toSql, mapping::mapResult)

    fun exec(statment: String): SQLTransaction<I, J, Unit> =
            addAction(SQLAction.Exec(statment))

    fun dropTable(tableName: String): SQLTransaction<I, J, Unit> =
            exec("DROP TABLE $tableName")

    fun dropTableIfExists(tableName: String): SQLTransaction<I, J, Unit> =
            exec("DROP TABLE IF EXISTS $tableName")

    fun deleteAll(tableName: (O) -> String): SQLTransaction<I, J, Unit> =
            update({ o -> SQLStatement.Simple("DELETE FROM ${tableName(o)}") }, { a, b -> TryUtil.unitSuccess })

    fun <K> mapAsync(mapper: (O) -> Future<K>): SQLTransaction<I, J, K> =
            addAction(SQLAction.MapAsync(mapper))

    fun <K> map(mapper: (O) -> K): SQLTransaction<I, J, K> =
            addAction(SQLAction.Map(mapper))

    fun <L, K> flatMap(next: SQLTransaction<O, L, K>): SQLTransaction<I, J, K> =
            addAction(SQLAction.Nested(next))
}