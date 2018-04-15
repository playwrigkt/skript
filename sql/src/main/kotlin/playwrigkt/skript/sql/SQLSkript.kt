package playwrigkt.skript.sql

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.ex.joinTry
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SQLTroupe

sealed class SQLSkript<IN, OUT>: Skript<IN, OUT, SQLTroupe> {

    companion object {
        private fun <IN, OUT, C: SQLCommand, R: SQLResult, Troupe> sql(toSql: Skript<IN, C, Troupe>,
                                                                       sql: SQLSkript<C, R>,
                                                                       mapResult: Skript<R, OUT, Troupe>): Skript<IN, OUT, Troupe> where Troupe: SQLTroupe =
                toSql
                        .andThen(sql)
                        .andThen(mapResult)

        fun <IN, OUT, Troupe> query(toSql: Skript<IN, SQLCommand.Query, Troupe>,
                                    mapResult: Skript<SQLResult.Query, OUT, Troupe>): Skript<IN, OUT, Troupe> where Troupe: SQLTroupe =
                sql(toSql, Query, mapResult)

        fun <IN, OUT, Troupe> update(toSql: Skript<IN, SQLCommand.Update, Troupe>,
                                     mapResult: Skript<SQLResult.Update, OUT, Troupe>): Skript<IN, OUT, Troupe> where Troupe: SQLTroupe =
                sql(toSql, Update, mapResult)

        fun <IN, OUT, Troupe> exec(toSql: Skript<IN, SQLCommand.Exec, Troupe>,
                                   mapResult: Skript<SQLResult.Void, OUT, Troupe>): Skript<IN, OUT, Troupe> where Troupe: SQLTroupe =
                sql(toSql, Exec, mapResult)

        fun <IN, OUT> query(mapping: SQLMapping<IN, OUT, SQLCommand.Query, SQLResult.Query>): Skript<IN, OUT, SQLTroupe> =
                Skript.identity<IN, SQLTroupe>()
                        .split(query(Skript.map(mapping::toSql), Skript.identity()))
                        .joinTry(mapping::mapResult)

        fun <IN, OUT> update(mapping: SQLMapping<IN, OUT, SQLCommand.Update, SQLResult.Update>): Skript<IN, OUT, SQLTroupe> =
                Skript.identity<IN, SQLTroupe>()
                        .split(update(Skript.map(mapping::toSql), Skript.identity()))
                        .joinTry(mapping::mapResult)

        fun <IN, OUT> exec(mapping: SQLMapping<IN, OUT, SQLCommand.Exec, SQLResult.Void>): Skript<IN, OUT, SQLTroupe> =
                Skript.identity<IN, SQLTroupe>()
                        .split(exec(Skript.map(mapping::toSql), Skript.identity()))
                        .joinTry(mapping::mapResult)
    }


    object Query: SQLSkript<SQLCommand.Query, SQLResult.Query>() {
        override fun run(i: SQLCommand.Query, troupe: SQLTroupe): AsyncResult<SQLResult.Query> {
            return troupe.getSQLPerformer()
                    .flatMap { sqlPerformer -> sqlPerformer.query(i) }
                    .recover { AsyncResult.failed(SQLError.OnCommand(i, it)) }
        }
    }

    object Update: SQLSkript<SQLCommand.Update, SQLResult.Update>() {
        override fun run(i: SQLCommand.Update, troupe: SQLTroupe): AsyncResult<SQLResult.Update> {
            return troupe.getSQLPerformer()
                    .flatMap { sqlPerformer -> sqlPerformer.update(i) }
                    .recover { AsyncResult.failed(SQLError.OnCommand(i, it)) }
        }
    }

    object Exec: SQLSkript<SQLCommand.Exec, SQLResult.Void>() {
        override fun run(i: SQLCommand.Exec, troupe: SQLTroupe): AsyncResult<SQLResult.Void> {
            return troupe.getSQLPerformer()
                    .flatMap { sqlPerformer -> sqlPerformer.exec(i) }
                    .recover { AsyncResult.failed(SQLError.OnCommand(i, it)) }
        }
    }

    protected fun <R> handleFailure(tri: Try<R>): AsyncResult<R> =
            when(tri) {
                is Try.Failure -> AsyncResult.failed(tri.throwable)
                is Try.Success -> AsyncResult.succeeded(tri.get())
            }
}





