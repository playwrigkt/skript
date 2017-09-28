package dev.yn.playground.sql

import dev.yn.playground.task.Task
import io.vertx.core.Future
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
import org.funktionale.either.Either
import org.funktionale.tries.Try

/**
 * Encapsulates all of the actions that can be run against the database.  These are chained together to create a Transaction.
 *
 * You can use nested transactions and the client will not generate extra begin/commit commands thereby maintaining the
 * outermost actionChain.
 *
 * You can also include Non SQL tasks in the transaction and they will affect whether the transction is committed or
 * rolledback, pending success
 *
 *
 * I input Type
 * O input Type
 */
sealed class SQLAction<I, O> {
    abstract fun run(i: I, connection: SQLConnection): Future<O>

    open fun <U> andThen(next: SQLAction<O, U>): SQLAction<I, U> =
            Link(this, next)

    internal data class DoWithConnection<I, O>(val action: (I, SQLConnection) -> Future<O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return action(i, connection)
        }
    }

    internal data class Link<I, J, O>(val head: SQLAction<I, J>, val tail: SQLAction<J, O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return head.run(i, connection).compose { tail.run(it, connection) }
        }

        override fun <U> andThen(next: SQLAction<O, U>): SQLAction<I, U> {
            return Link(head, tail.andThen(next))
        }
    }

    internal data class Query<I, O>(val mapping: QuerySQLMapping<I, O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            val sqlStatement = mapping.toSql(i)
            val sqlFuture = Future.future<ResultSet>()
            when(sqlStatement) {
                is SQLStatement.Parameterized -> connection.queryWithParams(sqlStatement.query, sqlStatement.params, sqlFuture.completer())
                is SQLStatement.Simple -> connection.query(sqlStatement.query, sqlFuture.completer())
            }
            return sqlFuture
                    .map { mapping.mapResult(i, it) }
                    .compose { handleFailure(it) }
                    .recover { Future.failedFuture<O>(SQLError.OnStatement(sqlStatement, it)) }
        }
    }

    internal data class Update<I, O>(val mapping: UpdateSQLMapping<I, O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            val sqlStatement = mapping.toSql(i)
            val sqlFuture = Future.future<UpdateResult>()
            when(sqlStatement) {
                is SQLStatement.Parameterized -> connection.updateWithParams(sqlStatement.query, sqlStatement.params, sqlFuture.completer())
                is SQLStatement.Simple -> connection.update(sqlStatement.query, sqlFuture.completer())
            }
            return sqlFuture
                    .map { mapping.mapResult(i, it) }
                    .compose { handleFailure(it) }
                    .recover { Future.failedFuture<O>(SQLError.OnStatement(sqlStatement, it)) }
        }
    }

    internal data class Exec<I>(val statement: String): SQLAction<I, I>() {
        override fun run(i: I, connection: SQLConnection): Future<I> {
            val sqlFuture = Future.future<Void>()
            connection.execute(statement, sqlFuture.completer())
            return sqlFuture
                    .map { i }
                    .recover { Future.failedFuture<I>(SQLError.OnStatement(SQLStatement.Simple(statement), it)) }
        }
    }

    internal data class Map<I, O>(val mapper: (I) -> O): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return handleFailure(Try { mapper(i) })
        }
    }

    internal data class MapTry<I, O>(val mapper: (I) -> Try<O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            try {
                return handleFailure(this.mapper(i))
            } catch(t: Throwable) {
                return Future.failedFuture(t)
            }
        }
    }

    internal data class MapTask<I, O>(val task: Task<I, O>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return task.run(i)
        }
    }

    internal data class WhenRight<I, J, O>(val doOptionally: SQLAction<J, O>, val whenRight: SQLAction<I, Either<O, J>>): SQLAction<I, O>() {
        override fun run(i: I, connection: SQLConnection): Future<O> {
            return whenRight.run(i, connection)
                    .compose {
                        when(it) {
                            is Either.Left -> it.left().get().let { Future.succeededFuture(it) }
                            is Either.Right -> it.right().get().let { doOptionally.run(it, connection) }
                        }
                    }
        }
    }

    internal data class WhenNonNull<I, J>(val doOptionally: SQLAction<J, I>, val whenNonNull: SQLAction<I, J?>): SQLAction<I, I>() {
        override fun run(i: I, connection: SQLConnection): Future<I> {
            return whenNonNull.run(i, connection)
                    .compose {
                        when(it) {
                            null -> Future.succeededFuture(i)
                            else -> doOptionally.run(it, connection)
                        }
                    }
        }
    }

    internal data class WhenTrue<I>(val doOptionally: SQLAction<I, I>, val whenTrue: SQLAction<I, Boolean>): SQLAction<I, I>() {
        override fun run(i: I, connection: SQLConnection): Future<I> {
            return whenTrue.run(i, connection)
                    .compose {
                        when(it) {
                            true -> doOptionally.run(i, connection)
                            else -> Future.succeededFuture(i)
                        }
                    }
        }
    }

    protected fun <R> handleFailure(tri: Try<R>): Future<R> =
            when(tri) {
                is Try.Failure -> Future.failedFuture(tri.throwable)
                is Try.Success -> Future.succeededFuture(tri.get())
            }
}