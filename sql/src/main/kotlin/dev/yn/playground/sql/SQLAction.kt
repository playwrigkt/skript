package dev.yn.playground.sql

import dev.yn.playground.util.TryUtil
import io.vertx.core.Future
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
import org.funktionale.tries.Try

/**
 * Encapsulates all of the actions that can be run against the database.  These are chained together to create a Transaction.
 *
 * You can use nested transactions and the client will not generate extra begin/commit commands thereby maintaining the
 * outermost transaction.
 *
 * I input Type
 * O output Type
 */
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

        override fun toString(): String =
                "SQLAction.Query(toSql=$toSql,mapResult=$mapResult)"
    }

    class Map<I, O>(val mapper: (I) -> O): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return Future.succeededFuture(this.mapper(i))
        }

        override fun toString(): String =
                "SQLAction.Map(mapper:$mapper)"
    }

    class MapAsync<I, O>(val mapper: (I) -> Future<O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return mapper(i)
        }

        override fun toString(): String =
                "SQLAction.MapAsync(mapper:$mapper)"
    }

    class Update<I, O>(val toSql: (I) -> SQLStatement, val mapResult: (I, UpdateResult) -> Try<O>, val expectedUpdates: Int? = null): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            val sqlStatement = this.toSql(i)
            val sqlFuture = Future.future<UpdateResult>()
            when(sqlStatement) {
                is SQLStatement.Parameterized -> connection.updateWithParams(sqlStatement.query, sqlStatement.params, sqlFuture.completer())
                is SQLStatement.Simple -> connection.update(sqlStatement.query, sqlFuture.completer())
            }
            return sqlFuture
                    .map { mapResult(i, it) }
                    .compose { TryUtil.handleFailure(it) }
                    .recover { Future.failedFuture<O>(SQLError.OnStatement(sqlStatement, it)) }
        }

        override fun toString(): String = "SQLAction.Update(toSql=$toSql,mapResult=$mapResult)"
    }

    class Exec<I>(val statement: String): SQLAction<I, I>() {
        override fun run(i: I, connection: SQLConnection): Future<I> {
            val sqlFuture = Future.future<Void>()
            connection.execute(statement, sqlFuture.completer())
            return sqlFuture
                    .map { i }
                    .recover { Future.failedFuture<I>(SQLError.OnStatement(SQLStatement.Simple(statement), it)) }
        }

        override fun toString(): String = "SQLAction.Exec(statement=$statement)"
    }
    class Nested<I, O>(val chain: SQLTransaction<I, O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return chain.run(i, connection)
        }

        override fun toString(): String = "SQLAction.Nested(chain=$chain)"
    }

    abstract fun run(i: I, connection: SQLConnection): Future<O>
}