package dev.yn.playground.sql

import dev.yn.playground.task.Task
import dev.yn.playground.util.TryUtil
import io.vertx.core.Future
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult

/**
 * Encapsulates all of the actions that can be run against the database.  These are chained together to create a Transaction.
 *
 * You can use nested transactions and the client will not generate extra begin/commit commands thereby maintaining the
 * outermost actionChain.
 *
 * I input Type
 * O input Type
 */
sealed class SQLAction<I, O> {
    internal class Query<I, O>(val mapping: QuerySQLMapping<I, O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            val sqlStatement = mapping.toSql(i)
            val sqlFuture = Future.future<ResultSet>()
            when(sqlStatement) {
                is SQLStatement.Parameterized -> connection.queryWithParams(sqlStatement.query, sqlStatement.params, sqlFuture.completer())
                is SQLStatement.Simple -> connection.query(sqlStatement.query, sqlFuture.completer())
            }
            return sqlFuture
                    .map { mapping.mapResult(i, it) }
                    .compose { TryUtil.handleFailure(it) }
                    .recover { Future.failedFuture<O>(SQLError.OnStatement(sqlStatement, it)) }
        }

        override fun toString(): String =
                "SQLAction.Query(mapping=$mapping)"
    }

    internal class Map<I, O>(val mapper: (I) -> O): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return Future.succeededFuture(this.mapper(i))
        }

        override fun toString(): String =
                "SQLAction.Map(mapper:$mapper)"
    }

    internal class MapAsync<I, O>(val mapper: (I) -> Future<O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return mapper(i)
        }

        override fun toString(): String =
                "SQLAction.MapAsync(mapper:$mapper)"
    }

    internal class MapTask<I, O>(val task: Task<I, O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return task.run(i)
        }

        override fun toString(): String =
                "SQLAction.MapTask(task=$task)"

    }


    internal class Update<I, O>(val mapping: UpdateSQLMapping<I, O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            val sqlStatement = mapping.toSql(i)
            val sqlFuture = Future.future<UpdateResult>()
            when(sqlStatement) {
                is SQLStatement.Parameterized -> connection.updateWithParams(sqlStatement.query, sqlStatement.params, sqlFuture.completer())
                is SQLStatement.Simple -> connection.update(sqlStatement.query, sqlFuture.completer())
            }
            return sqlFuture
                    .map { mapping.mapResult(i, it) }
                    .compose { TryUtil.handleFailure(it) }
                    .recover { Future.failedFuture<O>(SQLError.OnStatement(sqlStatement, it)) }
        }

        override fun toString(): String = "SQLAction.Update(mapping=$mapping)"
    }

    internal class Exec<I>(val statement: String): SQLAction<I, I>() {
        override fun run(i: I, connection: SQLConnection): Future<I> {
            val sqlFuture = Future.future<Void>()
            connection.execute(statement, sqlFuture.completer())
            return sqlFuture
                    .map { i }
                    .recover { Future.failedFuture<I>(SQLError.OnStatement(SQLStatement.Simple(statement), it)) }
        }

        override fun toString(): String = "SQLAction.Exec(statement=$statement)"
    }
    internal class Nested<I, O>(val chain: SQLActionChain<I, O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return chain.run(i, connection)
        }

        override fun toString(): String = "SQLAction.Nested(chain=$chain)"
    }

    abstract fun run(i: I, connection: SQLConnection): Future<O>
}