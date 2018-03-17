package dev.yn.playground.ex

import dev.yn.playground.Skript
import dev.yn.playground.context.SQLTaskContext
import dev.yn.playground.sql.*

fun <C: SQLTaskContext<*>> Skript<Unit, Unit, C>.dropTableIfExists(tableName: String) =
        this.andThen(SQLSkript.exec(SQLMapping.Companion.exec("DROP TABLE IF EXISTS $tableName")))

fun dropTableIfExists(tableName: String) =
        SQLSkript.exec(SQLMapping.Companion.exec("DROP TABLE IF EXISTS $tableName"))

fun <C: SQLTaskContext<*>> Skript<Unit, Unit, C>.deleteAll(tableName: String) =
        this.andThen(dev.yn.playground.sql.SQLSkript.exec(dev.yn.playground.sql.SQLMapping.Companion.exec("DELETE FROM $tableName")))

fun deleteAll(tableName: String) =
        SQLSkript.exec(SQLMapping.Companion.exec("DELETE FROM $tableName"))

fun <I, O, J, C: SQLTaskContext<*>> Skript<I, O, C>.query(mapping: SQLQueryMapping<O, J>) =
        this.andThen(SQLSkript.query(mapping))

fun <I, O, J, C: SQLTaskContext<*>> Skript<I, O, C>.update(mapping: SQLUpdateMapping<O, J>) =
        this.andThen(SQLSkript.update(mapping))

fun <I, O, J, C: SQLTaskContext<*>> Skript<I, O, C>.exec(mapping: SQLExecMapping<O, J>) =
        this.andThen(SQLSkript.exec(mapping))