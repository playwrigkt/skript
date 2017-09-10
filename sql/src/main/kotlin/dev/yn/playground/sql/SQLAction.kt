package dev.yn.playground.sql

import dev.yn.playground.util.TryUtil
import io.vertx.core.Future
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
import org.funktionale.tries.Try

sealed class SQLAction<I, O> {
    class Query<I, O>(val toSql: (I) -> SQLStatement, val mapResult: (I, ResultSet) -> Try<O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            val sqlStatement = toSql(i)
            val sqlFuture = Future.future<ResultSet>()
            when(sqlStatement) {
                is SQLStatement.Parameterized -> connection.queryWithParams(sqlStatement.query, sqlStatement.params, sqlFuture.completer())
                is SQLStatement.Simple -> connection.query(sqlStatement.query, sqlFuture.completer())
            }
            return sqlFuture
                    .map { mapResult(i, it) }
                    .compose { TryUtil.handleFailure(it) }
                    .recover { Future.failedFuture<O>(SQLError.OnStatement(sqlStatement, it)) }
        }
    }

    class Map<I, O>(val mapper: (I) -> O): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return Future.succeededFuture(this.mapper(i))
        }
    }

    class FlatMap<I, O>(val mapper: (I) -> Future<O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return mapper(i)
        }
    }

    class Update<I, O>(val toSql: (I) -> SQLStatement, val mapResult: (I, UpdateResult) -> Try<O>, val expectedUpdates: Int? = 1): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            val sqlStatement = this.toSql(i)
            val sqlFuture = Future.future<UpdateResult>()
            when(sqlStatement) {
                is SQLStatement.Parameterized -> connection.updateWithParams(sqlStatement.query, sqlStatement.params, sqlFuture.completer())
                is SQLStatement.Simple -> connection.update(sqlStatement.query, sqlFuture.completer())
            }
            return sqlFuture
                    .compose { updateResult ->
                        if(expectedUpdates?.let { updateResult.updated == it }?:(updateResult.updated > 0)) {
                            Future.succeededFuture<Try<O>>(this.mapResult(i, updateResult))
                        } else {
                            Future.failedFuture<Try<O>>(SQLError.UpdateFailed(this))
                        } }
                    .compose { TryUtil.handleFailure(it) }
                    .recover { Future.failedFuture<O>(SQLError.OnStatement(sqlStatement, it)) }
        }
    }

    class Exec<I>(val statement: String): SQLAction<I, Unit>() {
        override fun run(i: I, connection: SQLConnection): Future<Unit> {
            val sqlFuture = Future.future<Void>()
            connection.execute(statement, sqlFuture.completer())
            return sqlFuture
                    .map {}
                    .recover { Future.failedFuture<Unit>(SQLError.OnStatement(SQLStatement.Simple(statement), it)) }
        }
    }
    class Chain<I, O>(val chain: SQLTransaction<I, Any, O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return chain.run(i, connection)
        }
    }

    abstract fun run(i: I, connection: SQLConnection): Future<O>
}