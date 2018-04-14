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

fun <I, O, J, Troupe> Skript<I, O, Troupe>.query(mapping: SQLQueryMapping<O, J>) where Troupe: SQLTroupe =
        this.andThen(SQLSkript.query(mapping))

fun <I, O, J, Troupe> Skript<I, O, Troupe>.update(mapping: SQLUpdateMapping<O, J>) where Troupe: SQLTroupe =
        this.andThen(SQLSkript.update(mapping))

fun <I, O, J, Troupe> Skript<I, O, Troupe>.exec(mapping: SQLExecMapping<O, J>) where Troupe: SQLTroupe =
        this.andThen(SQLSkript.exec(mapping))

fun <I, Troupe> Skript<I, SQLCommand.Query, Troupe>.query() where Troupe: SQLTroupe =
        this.andThen(SQLSkript.Query)

fun <I, Troupe> Skript<I, SQLCommand.Update, Troupe>.update() where Troupe: SQLTroupe =
        this.andThen(SQLSkript.Update)

fun <I, Troupe> Skript<I, SQLCommand.Exec, Troupe>.exec() where Troupe: SQLTroupe =
        this.andThen(SQLSkript.Exec)
