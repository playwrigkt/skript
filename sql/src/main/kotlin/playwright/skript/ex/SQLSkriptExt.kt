package playwright.skript.ex

import playwright.skript.Skript
import playwright.skript.sql.*
import playwright.skript.stage.SQLStage

fun <Stage: SQLStage> Skript<Unit, Unit, Stage>.dropTableIfExists(tableName: String) =
        this.andThen(SQLSkript.exec(SQLMapping.exec("DROP TABLE IF EXISTS $tableName")))

fun dropTableIfExists(tableName: String) =
        SQLSkript.exec(SQLMapping.exec("DROP TABLE IF EXISTS $tableName"))

fun <Stage: SQLStage> Skript<Unit, Unit, Stage>.deleteAll(tableName: String) =
        this.andThen(SQLSkript.exec(SQLMapping.exec("DELETE FROM $tableName")))

fun deleteAll(tableName: String) =
        SQLSkript.exec(SQLMapping.exec("DELETE FROM $tableName"))

fun <I, O, J, Stage: SQLStage> Skript<I, O, Stage>.query(mapping: SQLQueryMapping<O, J>) =
        this.andThen(SQLSkript.query(mapping))

fun <I, O, J, Stage: SQLStage> Skript<I, O, Stage>.update(mapping: SQLUpdateMapping<O, J>) =
        this.andThen(SQLSkript.update(mapping))

fun <I, O, J, Stage: SQLStage> Skript<I, O, Stage>.exec(mapping: SQLExecMapping<O, J>) =
        this.andThen(SQLSkript.exec(mapping))