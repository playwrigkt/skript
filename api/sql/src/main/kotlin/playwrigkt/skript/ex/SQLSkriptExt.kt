package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.sql.*
import playwrigkt.skript.troupe.SQLTroupe

fun <Troupe> Skript<Unit, Unit, Troupe>.dropTableIfExists(tableName: String) where Troupe: SQLTroupe =
        this.andThen(SQLSkript.exec(SQLMapping.exec("DROP TABLE IF EXISTS $tableName")))

fun dropTableIfExists(tableName: String): Skript<Unit, Unit, SQLTroupe> =
        SQLSkript.exec(SQLMapping.exec("DROP TABLE IF EXISTS $tableName"))

fun <Troupe> Skript<Unit, Unit, Troupe>.deleteAll(tableName: String): Skript<Unit, Unit, Troupe> where Troupe: SQLTroupe =
        this.andThen(SQLSkript.exec(SQLMapping.exec("DELETE FROM $tableName")))

fun deleteAll(tableName: String): Skript<Unit, Unit, SQLTroupe> =
        SQLSkript.exec(SQLMapping.exec("DELETE FROM $tableName"))

/*
 * Chain a sql query skript and handle mapping to the query and from the result
 */
fun <I, O, J, Troupe> Skript<I, O, Troupe>.query(mapping: SQLQueryMapping<O, J>) where Troupe: SQLTroupe =
        this.andThen(SQLSkript.query(mapping))

/**
 * Chain a sql update skript and handle mapping to the query and from the result
 */
fun <I, O, J, Troupe> Skript<I, O, Troupe>.update(mapping: SQLUpdateMapping<O, J>) where Troupe: SQLTroupe =
        this.andThen(SQLSkript.update(mapping))

/**
 * Chain a sql Exec skript and handle mapping to the query and from the result
 */
fun <I, O, J, Troupe> Skript<I, O, Troupe>.exec(mapping: SQLExecMapping<O, J>) where Troupe: SQLTroupe =
        this.andThen(SQLSkript.exec(mapping))

/**
 * Chain a sql Query skript
 */
fun <I, Troupe> Skript<I, SQLCommand.Query, Troupe>.query() where Troupe: SQLTroupe =
        this.andThen(SQLSkript.Query)

/**
 * Chain a sql Update Skript
 */
fun <I, Troupe> Skript<I, SQLCommand.Update, Troupe>.update() where Troupe: SQLTroupe =
        this.andThen(SQLSkript.Update)

/**
 * CHain a sql Exec Skript
 */
fun <I, Troupe> Skript<I, SQLCommand.Exec, Troupe>.exec() where Troupe: SQLTroupe =
        this.andThen(SQLSkript.Exec)
