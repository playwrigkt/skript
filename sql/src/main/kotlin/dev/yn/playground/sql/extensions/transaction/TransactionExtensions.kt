package dev.yn.playground.sql.extensions.transaction

import dev.yn.playground.sql.*
import dev.yn.playground.util.TryUtil
import io.vertx.core.Future
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.UpdateResult
import org.funktionale.tries.Try

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

fun <I> deleteAll(tableName: (I) -> String): SQLTransaction<I, Unit, Unit> =
        update({ i -> SQLStatement.Simple("DELETE FROM ${tableName(i)}") }, { a, b -> TryUtil.unitSuccess })

fun deleteAll(tableName: String): SQLTransaction<Unit, Unit, Unit> =
        update({ SQLStatement.Simple("DELETE FROM $tableName") }, { a, b -> TryUtil.unitSuccess })

fun <I, J, K, O> SQLTransaction<I, J, K>.query(toSql: (K) -> SQLStatement, mapResult: (K, ResultSet) -> Try<O>): SQLTransaction<I, J, O> =
        addAction(SQLAction.Query(toSql, mapResult))

fun <I, J, K, O> SQLTransaction<I, J, K>.query(mapping: QuerySQLMapping<K, O>): SQLTransaction<I, J, O> =
        query(mapping::toSql, mapping::mapResult)

fun <I, J, K, O> SQLTransaction<I, J, K>.update(toSql: (K) -> SQLStatement, mapResult: (K, UpdateResult) -> Try<O>): SQLTransaction<I, J, O> =
        addAction(SQLAction.Update(toSql, mapResult))

fun <I, J, K, O> SQLTransaction<I, J, K>.update(mapping: UpdateSQLMapping<K, O>): SQLTransaction<I, J, O> =
        update(mapping::toSql, mapping::mapResult)

fun <I, J, K> SQLTransaction<I, J, K>.exec(statment: String): SQLTransaction<I, J, Unit> =
        addAction(SQLAction.Exec(statment))

fun <I, J, K> SQLTransaction<I, J, K>.dropTable(tableName: String): SQLTransaction<I, J, Unit> =
        exec("DROP TABLE $tableName")

fun <I, J, K> SQLTransaction<I, J, K>.deleteAll(tableName: (K) -> String): SQLTransaction<I, J, Unit> =
        update({ o -> SQLStatement.Simple("DELETE FROM ${tableName(o)}") }, { a, b -> TryUtil.unitSuccess })

fun <I, J, K, O> SQLTransaction<I, J, K>.flatMap(mapper: (K) -> Future<O>): SQLTransaction<I, J, O> =
        addAction(SQLAction.FlatMap(mapper))

fun <I, J, K, O> SQLTransaction<I, J, K>.map(mapper: (K) -> O): SQLTransaction<I, J, O> =
        addAction(SQLAction.Map(mapper))