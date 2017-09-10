package dev.yn.playground.sql

import io.vertx.core.Future
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
interface SQLMapping<I, O, RS> {
    fun toSql(i: I): SQLStatement
    fun mapResult(i: I, rs: RS): O
}
interface UpdateSQLMapping<I, O>: SQLMapping<I, O, UpdateResult>
interface QuerySQLMapping<I, O>: SQLMapping<I, O, ResultSet>

sealed class SQLTransaction<I, J, O> {
    companion object {
        fun <I, O> new(action: SQLAction<I, O>): SQLTransaction<I, O, O> {
            return EndLink(action)
        }

        fun <I, O> query(toSql: (I) -> SQLStatement, mapResult: (I, ResultSet) -> O): SQLTransaction<I, O, O> =
                new(SQLAction.Query(toSql, mapResult))

        fun <I, O> query(mapping: QuerySQLMapping<I, O>): SQLTransaction<I, O, O> =
                query(mapping::toSql, mapping::mapResult)

        fun <I, O> update(toSql: (I) -> SQLStatement, mapResult: (I, UpdateResult) -> O): SQLTransaction<I, O, O> =
                new(SQLAction.Update(toSql, mapResult))

        fun <I, O> update(mapping: UpdateSQLMapping<I, O>): SQLTransaction<I, O, O> =
                update(mapping::toSql, mapping::mapResult)

        fun exec(statment: String): SQLTransaction<Unit, Unit, Unit> =
                new(SQLAction.Exec(statment))

        fun dropTable(tableName: String): SQLTransaction<Unit, Unit, Unit> =
                exec("DROP TABLE $tableName")

        fun deleteAll(tableName: String): SQLTransaction<Unit, Unit, Unit> =
                exec("DELETE FROM $tableName")

    }

    fun <K> query(toSql: (O) -> SQLStatement, mapResult: (O, ResultSet) -> K): SQLTransaction<I, J, K> =
            addAction(SQLAction.Query(toSql, mapResult))

    fun <K> query(mapping: QuerySQLMapping<O, K>): SQLTransaction<I, J, K> =
            query(mapping::toSql, mapping::mapResult)

    fun <K> update(toSql: (O) -> SQLStatement, mapResult: (O, UpdateResult) -> K): SQLTransaction<I, J, K> =
            addAction(SQLAction.Update(toSql, mapResult))

    fun <K> update(mapping: UpdateSQLMapping<O, K>): SQLTransaction<I, J, K> =
            update(mapping::toSql, mapping::mapResult)

    fun exec(statment: String): SQLTransaction<I, J, Unit> =
            addAction(SQLAction.Exec(statment))

    fun dropTable(tableName: String): SQLTransaction<I, J, Unit> =
            exec("DROP TABLE $tableName")

    fun deleteAll(tableName: String): SQLTransaction<I, J, Unit> =
            exec("DELETE FROM $tableName")

    fun <K> flatMap(mapper: (O) -> Future<K>): SQLTransaction<I, J, K> =
            addAction(SQLAction.FlatMap(mapper))

    fun <K> map(mapper: (O) -> K): SQLTransaction<I, J, K> =
            addAction(SQLAction.Map(mapper))

    abstract fun <U> addAction(action: SQLAction<O, U>): SQLTransaction<I, J, U>
    abstract fun run(i: I, connection: SQLConnection): Future<O>

    data class EndLink<I, O>(val action: SQLAction<I, O>): SQLTransaction<I, O, O>() {
        override fun <U> addAction(action: SQLAction<O, U>): SQLTransaction<I, O, U> =
                ActionLink(this.action, EndLink(action))

        override fun run(i: I, connection: SQLConnection): Future<O> =
                action.run(i, connection)
    }

    data class ActionLink<I, J, K, O>(val action: SQLAction<I, J>,
                                      val next: SQLTransaction<J, K, O>): SQLTransaction<I, J, O>() {
        override fun <U> addAction(action: SQLAction<O, U>): SQLTransaction<I, J, U> =
                ActionLink(this.action, next.addAction(action))

        override fun run(i: I, connection: SQLConnection): Future<O> =
                action.run(i, connection)
                        .compose { u -> next.run(u, connection) }
    }
}