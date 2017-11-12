package dev.yn.playground.sql.ext

import dev.yn.playground.sql.*
import dev.yn.playground.sql.context.SQLTaskContext
import dev.yn.playground.task.Task

fun <C: SQLTaskContext<*>> Task<Unit, Unit, C>.dropTableIfExists(tableName: String) =
        this.andThen(SQLTask.exec(SQLMapping.Companion.exec("DROP TABLE IF EXISTS $tableName")))

fun <C: SQLTaskContext<*>> dropTableIfExists(tableName: String) =
        SQLTask.exec<Unit, Unit, C>(SQLMapping.Companion.exec("DROP TABLE IF EXISTS $tableName"))

fun <C: SQLTaskContext<*>> Task<Unit, Unit, C>.deleteAll(tableName: String) =
        this.andThen(dev.yn.playground.sql.SQLTask.exec(dev.yn.playground.sql.SQLMapping.Companion.exec("DELETE FROM $tableName")))

fun <C: SQLTaskContext<*>> deleteAll(tableName: String) =
        SQLTask.exec<Unit, Unit, C>(SQLMapping.Companion.exec("DELETE FROM $tableName"))

fun <I, O, J, C: SQLTaskContext<*>> Task<I, O, C>.query(mapping: SQLQueryMapping<O, J>) =
        this.andThen(SQLTask.query<O, J, C>(mapping))

fun <I, O, J, C: SQLTaskContext<*>> Task<I, O, C>.update(mapping: SQLUpdateMapping<O, J>) =
        this.andThen(SQLTask.update<O, J, C>(mapping))

fun <I, O, J, C: SQLTaskContext<*>> Task<I, O, C>.exec(mapping: SQLExecMapping<O, J>) =
        this.andThen(SQLTask.exec<O, J, C>(mapping))