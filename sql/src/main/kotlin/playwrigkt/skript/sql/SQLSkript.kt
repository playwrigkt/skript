package playwrigkt.skript.sql

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SQLTroupe

sealed class SQLSkript<IN, OUT>: Skript<IN, OUT, SQLTroupe> {

    companion object {
        fun <IN, OUT> query(mapping: SQLMapping<IN, OUT, SQLCommand.Query, SQLResult.Query>): SQLSkript<IN, OUT> = Query(mapping)
        fun <IN, OUT> update(mapping: SQLMapping<IN, OUT, SQLCommand.Update, SQLResult.Update>): SQLSkript<IN, OUT> = Update(mapping)
        fun <IN, OUT> exec(mapping: SQLMapping<IN, OUT, SQLCommand.Exec, SQLResult.Void>): SQLSkript<IN, OUT> = Exec(mapping)
    }

    abstract val mapping: SQLMapping<IN, OUT, *, *>

    private data class Query<IN, OUT>(override val mapping: SQLMapping<IN, OUT, SQLCommand.Query, SQLResult.Query>): SQLSkript<IN, OUT>() {
        override fun run(i: IN, troupe: SQLTroupe): AsyncResult<OUT> {
            val sqlCommand = mapping.toSql(i)
            return troupe.getSQLPerformer().query(sqlCommand)
                    .map { mapping.mapResult(i, it) }
                    .flatMap(this::handleFailure)
                    .recover { AsyncResult.failed(SQLError.OnCommand(sqlCommand, it)) }
        }
    }

    private data class Update<IN, OUT>(override val mapping: SQLMapping<IN, OUT, SQLCommand.Update, SQLResult.Update>): SQLSkript<IN, OUT>() {
        override fun run(i: IN, troupe: SQLTroupe): AsyncResult<OUT> {
            val sqlCommand = mapping.toSql(i)
            return troupe.getSQLPerformer().update(sqlCommand)
                    .map { mapping.mapResult(i, it) }
                    .flatMap(this::handleFailure)
                    .recover { AsyncResult.failed(SQLError.OnCommand(sqlCommand, it)) }
        }
    }

    private data class Exec<IN, OUT>(override val mapping: SQLMapping<IN, OUT, SQLCommand.Exec, SQLResult.Void>): SQLSkript<IN, OUT>() {
        override fun run(i: IN, troupe: SQLTroupe): AsyncResult<OUT> {
            val sqlCommand = mapping.toSql(i)
            return troupe.getSQLPerformer().exec(sqlCommand)
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





