package dev.yn.playground.sql

import dev.yn.playground.sql.context.SQLTaskContext
import dev.yn.playground.task.Task
import dev.yn.playground.task.result.AsyncResult
import org.funktionale.tries.Try

sealed class SQLTask<IN, OUT, C: SQLTaskContext<*>>: Task<IN, OUT, C> {
    companion object {
        fun <IN, OUT, C: SQLTaskContext<*>> query(mapping: SQLMapping<IN, OUT, SQLCommand.Query, SQLResult.Query>): SQLTask<IN, OUT, C> = Query(mapping)
        fun <IN, OUT, C: SQLTaskContext<*>> update(mapping: SQLMapping<IN, OUT, SQLCommand.Update, SQLResult.Update>): SQLTask<IN, OUT, C> = Update(mapping)
        fun <IN, OUT, C: SQLTaskContext<*>> exec(mapping: SQLMapping<IN, OUT, SQLCommand.Exec, SQLResult.Void>): SQLTask<IN, OUT, C> = Exec(mapping)
    }

    fun <U> query(mapping: SQLMapping<OUT, U, SQLCommand.Query, SQLResult.Query>): Task<IN, U, C> = this.andThen(Query(mapping))
    fun <U> update(mapping: SQLMapping<OUT, U, SQLCommand.Update, SQLResult.Update>): Task<IN, U, C> = this.andThen(Update(mapping))
    fun <U> exec(mapping: SQLMapping<OUT, U, SQLCommand.Exec, SQLResult.Void>): Task<IN, U, C> = this.andThen(Exec(mapping))

    abstract val mapping: SQLMapping<IN, OUT, *, *>
    private data class Query<IN, OUT, C: SQLTaskContext<*>>(override val mapping: SQLMapping<IN, OUT, SQLCommand.Query, SQLResult.Query>): SQLTask<IN, OUT, C>() {
        override fun run(i: IN, context: C): AsyncResult<OUT> {
            val sqlCommand = mapping.toSql(i)
            return context.getSQLExecutor().query(sqlCommand)
                    .map { mapping.mapResult(i, it) }
                    .flatMap(this::handleFailure)
                    .recover { AsyncResult.failed(SQLError.OnCommand(sqlCommand, it)) }
        }
    }

    private data class Update<IN, OUT, C: SQLTaskContext<*>>(override val mapping: SQLMapping<IN, OUT, SQLCommand.Update, SQLResult.Update>): SQLTask<IN, OUT, C>() {
        override fun run(i: IN, context: C): AsyncResult<OUT> {
            val sqlCommand = mapping.toSql(i)
            return context.getSQLExecutor().update(sqlCommand)
                    .map { mapping.mapResult(i, it) }
                    .flatMap(this::handleFailure)
                    .recover { AsyncResult.failed(SQLError.OnCommand(sqlCommand, it)) }
        }
    }

    private data class Exec<IN, OUT, C: SQLTaskContext<*>>(override val mapping: SQLMapping<IN, OUT, SQLCommand.Exec, SQLResult.Void>): SQLTask<IN, OUT, C>() {
        override fun run(i: IN, context: C): AsyncResult<OUT> {
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





