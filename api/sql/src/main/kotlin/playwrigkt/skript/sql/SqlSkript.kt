package playwrigkt.skript.sql

import arrow.core.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.ex.joinTry
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SqlTroupe

sealed class SqlSkript<IN, OUT>: Skript<IN, OUT, SqlTroupe> {

    companion object {
        private fun <IN, OUT, C: SqlCommand, R: SqlResult, Troupe> sql(toSql: Skript<IN, C, Troupe>,
                                                                       sql: SqlSkript<C, R>,
                                                                       mapResult: Skript<R, OUT, Troupe>): Skript<IN, OUT, Troupe> where Troupe: SqlTroupe =
                toSql
                        .andThen(sql)
                        .andThen(mapResult)

        fun <IN, OUT, Troupe> query(toSql: Skript<IN, SqlCommand.Query, Troupe>,
                                    mapResult: Skript<SqlResult.Query, OUT, Troupe>): Skript<IN, OUT, Troupe> where Troupe: SqlTroupe =
                sql(toSql, Query, mapResult)

        fun <IN, OUT, Troupe> update(toSql: Skript<IN, SqlCommand.Update, Troupe>,
                                     mapResult: Skript<SqlResult.Update, OUT, Troupe>): Skript<IN, OUT, Troupe> where Troupe: SqlTroupe =
                sql(toSql, Update, mapResult)

        fun <IN, OUT, Troupe> exec(toSql: Skript<IN, SqlCommand.Exec, Troupe>,
                                   mapResult: Skript<SqlResult.Void, OUT, Troupe>): Skript<IN, OUT, Troupe> where Troupe: SqlTroupe =
                sql(toSql, Exec, mapResult)

        fun <IN, OUT> query(mapping: SqlMapping<IN, OUT, SqlCommand.Query, SqlResult.Query>): Skript<IN, OUT, SqlTroupe> =
                Skript.identity<IN, SqlTroupe>()
                        .split(query(Skript.map(mapping::toSql), Skript.identity()))
                        .joinTry(mapping::mapResult)

        fun <IN, OUT> update(mapping: SqlMapping<IN, OUT, SqlCommand.Update, SqlResult.Update>): Skript<IN, OUT, SqlTroupe> =
                Skript.identity<IN, SqlTroupe>()
                        .split(update(Skript.map(mapping::toSql), Skript.identity()))
                        .joinTry(mapping::mapResult)

        fun <IN, OUT> exec(mapping: SqlMapping<IN, OUT, SqlCommand.Exec, SqlResult.Void>): Skript<IN, OUT, SqlTroupe> =
                Skript.identity<IN, SqlTroupe>()
                        .split(exec(Skript.map(mapping::toSql), Skript.identity()))
                        .joinTry(mapping::mapResult)
    }

    /**
     * Execute a sqlQuery and return a resultSet
     */
    object Query: SqlSkript<SqlCommand.Query, SqlResult.Query>() {
        override fun run(i: SqlCommand.Query, troupe: SqlTroupe): AsyncResult<SqlResult.Query> {
            return troupe.getSQLPerformer()
                    .flatMap { sqlPerformer -> sqlPerformer.query(i) }
                    .recover { AsyncResult.failed(SqlError.OnCommand(i, it)) }
        }
    }

    /**
     * Execute a sqlUpdate and return the number of rows updated
     */
    object Update: SqlSkript<SqlCommand.Update, SqlResult.Update>() {
        override fun run(i: SqlCommand.Update, troupe: SqlTroupe): AsyncResult<SqlResult.Update> {
            return troupe.getSQLPerformer()
                    .flatMap { sqlPerformer -> sqlPerformer.update(i) }
                    .recover { AsyncResult.failed(SqlError.OnCommand(i, it)) }
        }
    }

    /**
     * Execute a sql statement with no result
     */
    object Exec: SqlSkript<SqlCommand.Exec, SqlResult.Void>() {
        override fun run(i: SqlCommand.Exec, troupe: SqlTroupe): AsyncResult<SqlResult.Void> {
            return troupe.getSQLPerformer()
                    .flatMap { sqlPerformer -> sqlPerformer.exec(i) }
                    .recover { AsyncResult.failed(SqlError.OnCommand(i, it)) }
        }
    }

    protected fun <R> handleFailure(tri: Try<R>): AsyncResult<R> =
            when(tri) {
                is Try.Failure -> AsyncResult.failed(tri.exception)
                is Try.Success -> AsyncResult.succeeded(tri.value)
            }
}





