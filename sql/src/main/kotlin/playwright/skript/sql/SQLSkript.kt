package playwright.skript.sql

import org.funktionale.tries.Try
import playwright.skript.Skript
import playwright.skript.ex.andThen
import playwright.skript.result.AsyncResult
import playwright.skript.stage.SQLStage

fun <I, O, O2, C: SQLStage<*>> Skript<I, O, C>.query(mapping: SQLMapping<O, O2, SQLCommand.Query, SQLResult.Query>): Skript<I, O2, C> =
        this.andThen(SQLSkript.query(mapping))

fun <I, O, O2, C: SQLStage<*>> Skript<I, O, C>.update(mapping: SQLMapping<O, O2, SQLCommand.Update, SQLResult.Update>): Skript<I, O2, C> =
        this.andThen(SQLSkript.update(mapping))

fun <I, O, O2, C: SQLStage<*>> Skript<I, O, C>.exec(mapping: SQLMapping<O, O2, SQLCommand.Exec, SQLResult.Void>): Skript<I, O2, C> =
        this.andThen(SQLSkript.exec(mapping))

sealed class SQLSkript<IN, OUT>: Skript<IN, OUT, SQLStage<*>> {

    companion object {
        fun <IN, OUT> query(mapping: SQLMapping<IN, OUT, SQLCommand.Query, SQLResult.Query>): SQLSkript<IN, OUT> = Query(mapping)
        fun <IN, OUT> update(mapping: SQLMapping<IN, OUT, SQLCommand.Update, SQLResult.Update>): SQLSkript<IN, OUT> = Update(mapping)
        fun <IN, OUT> exec(mapping: SQLMapping<IN, OUT, SQLCommand.Exec, SQLResult.Void>): SQLSkript<IN, OUT> = Exec(mapping)
    }


    abstract val mapping: SQLMapping<IN, OUT, *, *>

    private data class Query<IN, OUT>(override val mapping: SQLMapping<IN, OUT, SQLCommand.Query, SQLResult.Query>): SQLSkript<IN, OUT>() {
        override fun run(i: IN, stage: SQLStage<*>): AsyncResult<OUT> {
            val sqlCommand = mapping.toSql(i)
            return stage.getSQLPerformer().query(sqlCommand)
                    .map { mapping.mapResult(i, it) }
                    .flatMap(this::handleFailure)
                    .recover { AsyncResult.failed(SQLError.OnCommand(sqlCommand, it)) }
        }
    }

    private data class Update<IN, OUT>(override val mapping: SQLMapping<IN, OUT, SQLCommand.Update, SQLResult.Update>): SQLSkript<IN, OUT>() {
        override fun run(i: IN, stage: SQLStage<*>): AsyncResult<OUT> {
            val sqlCommand = mapping.toSql(i)
            return stage.getSQLPerformer().update(sqlCommand)
                    .map { mapping.mapResult(i, it) }
                    .flatMap(this::handleFailure)
                    .recover { AsyncResult.failed(SQLError.OnCommand(sqlCommand, it)) }
        }
    }

    private data class Exec<IN, OUT>(override val mapping: SQLMapping<IN, OUT, SQLCommand.Exec, SQLResult.Void>): SQLSkript<IN, OUT>() {
        override fun run(i: IN, stage: SQLStage<*>): AsyncResult<OUT> {
            val sqlCommand = mapping.toSql(i)
            return stage.getSQLPerformer().exec(sqlCommand)
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





