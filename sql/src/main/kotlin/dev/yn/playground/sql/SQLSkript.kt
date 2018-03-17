package dev.yn.playground.sql

import dev.yn.playground.Skript
import dev.yn.playground.ex.andThen
import dev.yn.playground.context.SQLSkriptContext
import dev.yn.playground.result.AsyncResult
import org.funktionale.tries.Try

fun <I, O, O2, C: SQLSkriptContext<*>> Skript<I, O, C>.query(mapping: SQLMapping<O, O2, SQLCommand.Query, SQLResult.Query>): Skript<I, O2, C> =
        this.andThen(SQLSkript.query(mapping))

fun <I, O, O2, C: SQLSkriptContext<*>> Skript<I, O, C>.update(mapping: SQLMapping<O, O2, SQLCommand.Update, SQLResult.Update>): Skript<I, O2, C> =
        this.andThen(SQLSkript.update(mapping))

fun <I, O, O2, C: SQLSkriptContext<*>> Skript<I, O, C>.exec(mapping: SQLMapping<O, O2, SQLCommand.Exec, SQLResult.Void>): Skript<I, O2, C> =
        this.andThen(SQLSkript.exec(mapping))

sealed class SQLSkript<IN, OUT>: Skript<IN, OUT, SQLSkriptContext<*>> {

    companion object {
        fun <IN, OUT> query(mapping: SQLMapping<IN, OUT, SQLCommand.Query, SQLResult.Query>): SQLSkript<IN, OUT> = Query(mapping)
        fun <IN, OUT> update(mapping: SQLMapping<IN, OUT, SQLCommand.Update, SQLResult.Update>): SQLSkript<IN, OUT> = Update(mapping)
        fun <IN, OUT> exec(mapping: SQLMapping<IN, OUT, SQLCommand.Exec, SQLResult.Void>): SQLSkript<IN, OUT> = Exec(mapping)
    }


    abstract val mapping: SQLMapping<IN, OUT, *, *>

    private data class Query<IN, OUT>(override val mapping: SQLMapping<IN, OUT, SQLCommand.Query, SQLResult.Query>): SQLSkript<IN, OUT>() {
        override fun run(i: IN, context: SQLSkriptContext<*>): AsyncResult<OUT> {
            val sqlCommand = mapping.toSql(i)
            return context.getSQLExecutor().query(sqlCommand)
                    .map { mapping.mapResult(i, it) }
                    .flatMap(this::handleFailure)
                    .recover { AsyncResult.failed(SQLError.OnCommand(sqlCommand, it)) }
        }
    }

    private data class Update<IN, OUT>(override val mapping: SQLMapping<IN, OUT, SQLCommand.Update, SQLResult.Update>): SQLSkript<IN, OUT>() {
        override fun run(i: IN, context: SQLSkriptContext<*>): AsyncResult<OUT> {
            val sqlCommand = mapping.toSql(i)
            return context.getSQLExecutor().update(sqlCommand)
                    .map { mapping.mapResult(i, it) }
                    .flatMap(this::handleFailure)
                    .recover { AsyncResult.failed(SQLError.OnCommand(sqlCommand, it)) }
        }
    }

    private data class Exec<IN, OUT>(override val mapping: SQLMapping<IN, OUT, SQLCommand.Exec, SQLResult.Void>): SQLSkript<IN, OUT>() {
        override fun run(i: IN, context: SQLSkriptContext<*>): AsyncResult<OUT> {
            val sqlCommand = mapping.toSql(i)
            return context.getSQLExecutor().exec(sqlCommand)
                    .map { mapping.mapResult(i, it) }
                    .flatMap(this::handleFailure)
                    .recover { AsyncResult.failed(SQLError.OnCommand(sqlCommand, it)) }
        }
    }

    protected fun <R> handleFailure(tri: Try<R>): AsyncResult<R> =
            when(tri) {
                is Try.Failure -> AsyncResult.failed(tri.throwable)
                is Try.Success -> AsyncResult.succeeded(tri.get())
            }
}





