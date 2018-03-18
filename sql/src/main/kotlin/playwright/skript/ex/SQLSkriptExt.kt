package playwright.skript.ex

import playwright.skript.Skript
import playwright.skript.sql.*
import playwright.skript.stage.SQLStage

fun <C: SQLStage<*>> Skript<Unit, Unit, C>.dropTableIfExists(tableName: String) =
        this.andThen(SQLSkript.exec(SQLMapping.Companion.exec("DROP TABLE IF EXISTS $tableName")))

fun dropTableIfExists(tableName: String) =
        SQLSkript.exec(SQLMapping.Companion.exec("DROP TABLE IF EXISTS $tableName"))

fun <C: SQLStage<*>> Skript<Unit, Unit, C>.deleteAll(tableName: String) =
        this.andThen(playwright.skript.sql.SQLSkript.exec(playwright.skript.sql.SQLMapping.Companion.exec("DELETE FROM $tableName")))

fun deleteAll(tableName: String) =
        SQLSkript.exec(SQLMapping.Companion.exec("DELETE FROM $tableName"))

fun <I, O, J, C: SQLStage<*>> Skript<I, O, C>.query(mapping: SQLQueryMapping<O, J>) =
        this.andThen(SQLSkript.query(mapping))

fun <I, O, J, C: SQLStage<*>> Skript<I, O, C>.update(mapping: SQLUpdateMapping<O, J>) =
        this.andThen(SQLSkript.update(mapping))

fun <I, O, J, C: SQLStage<*>> Skript<I, O, C>.exec(mapping: SQLExecMapping<O, J>) =
        this.andThen(SQLSkript.exec(mapping))