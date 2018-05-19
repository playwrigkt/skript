package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.sql.*
import playwrigkt.skript.troupe.SqlTroupe

fun <Troupe> Skript<Unit, Unit, Troupe>.dropTableIfExists(tableName: String) where Troupe: SqlTroupe =
        this.andThen(SqlSkript.exec(SqlMapping.exec("DROP TABLE IF EXISTS $tableName")))

fun dropTableIfExists(tableName: String): Skript<Unit, Unit, SqlTroupe> =
        SqlSkript.exec(SqlMapping.exec("DROP TABLE IF EXISTS $tableName"))

fun <Troupe> Skript<Unit, Unit, Troupe>.deleteAll(tableName: String): Skript<Unit, Unit, Troupe> where Troupe: SqlTroupe =
        this.andThen(SqlSkript.exec(SqlMapping.exec("DELETE FROM $tableName")))

fun deleteAll(tableName: String): Skript<Unit, Unit, SqlTroupe> =
        SqlSkript.exec(SqlMapping.exec("DELETE FROM $tableName"))

/*
 * Chain a sql query skript and handle mapping to the query and from the result
 */
fun <I, O, J, Troupe> Skript<I, O, Troupe>.query(mapping: SqlQueryMapping<O, J>) where Troupe: SqlTroupe =
        this.andThen(SqlSkript.query(mapping))

/**
 * Chain a sql update skript and handle mapping to the query and from the result
 */
fun <I, O, J, Troupe> Skript<I, O, Troupe>.update(mapping: SqlUpdateMapping<O, J>) where Troupe: SqlTroupe =
        this.andThen(SqlSkript.update(mapping))

/**
 * Chain a sql Exec skript and handle mapping to the query and from the result
 */
fun <I, O, J, Troupe> Skript<I, O, Troupe>.exec(mapping: SqlExecMapping<O, J>) where Troupe: SqlTroupe =
        this.andThen(SqlSkript.exec(mapping))

/**
 * Chain a sql Query skript
 */
fun <I, Troupe> Skript<I, SqlCommand.Query, Troupe>.query() where Troupe: SqlTroupe =
        this.andThen(SqlSkript.Query)

/**
 * Chain a sql Update Skript
 */
fun <I, Troupe> Skript<I, SqlCommand.Update, Troupe>.update() where Troupe: SqlTroupe =
        this.andThen(SqlSkript.Update)

/**
 * CHain a sql Exec Skript
 */
fun <I, Troupe> Skript<I, SqlCommand.Exec, Troupe>.exec() where Troupe: SqlTroupe =
        this.andThen(SqlSkript.Exec)
