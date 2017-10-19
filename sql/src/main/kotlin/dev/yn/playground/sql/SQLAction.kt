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

    companion object {
        fun <I, O> doWithConnection(action: (I, SQLConnection) -> Future<O>): SQLAction<I, O> =
                DoWithConnection(action)

        fun <I, O> query(toSql: (I) -> SQLStatement, mapResult: (I, ResultSet) -> Try<O>): SQLAction<I, O> =
                query(QuerySQLMapping.create(toSql, mapResult))

        fun <I, O> query(mapping: QuerySQLMapping<I, O>): SQLAction<I, O> =
                Query(mapping)

        fun <I, O> update(toSql: (I) -> SQLStatement, mapResult: (I, UpdateResult) -> Try<O>): SQLAction<I, O> =
                update(UpdateSQLMapping.create(toSql, mapResult))

        fun <I, O> update(mapping: UpdateSQLMapping<I, O>): SQLAction<I, O> =
                Update(mapping)

        fun <I> exec(statment: String): SQLAction<I, I> =
                Exec(statment)

        fun <I> dropTable(tableName: String): SQLAction<I, I> =
                exec("DROP TABLE $tableName")

        fun <I> dropTableIfExists(tableName: String): SQLAction<I, I> =
                exec("DROP TABLE IF EXISTS $tableName")

        fun <I> deleteAll(tableName: (I) -> String): SQLAction<I, I> =
                update({ i -> SQLStatement.Simple("DELETE FROM ${tableName(i)}") }, { a, _ -> Try.Success(a) })

        fun <I> deleteAll(tableName: String): SQLAction<I, I> =
                update({ SQLStatement.Simple("DELETE FROM $tableName") }, { a, _ -> Try.Success(a) })

        fun <I, O> task(task: Task<I, O>): SQLAction<I, O> =
                MapTask(task)

        fun <I, O> map(mapper: (I) -> O): SQLAction<I, O> =
                Map(mapper)

        fun <I, O> mapTry(mapper: (I) -> Try<O>): SQLAction<I, O> =
                MapTry(mapper)

        fun <I, J, O> whenRight(doOptionally: SQLAction<J, O>, whenRight: (I) -> Either<O, J>): SQLAction<I, O> =
                WhenRight(doOptionally, map<I, Either<O, J>>(whenRight))

        fun <I, J> whenNonNull(doOptionally: SQLAction<J, I>, whenNonNull: (I) -> J?): SQLAction<I, I> =
                WhenNonNull(doOptionally, map<I, J?>(whenNonNull))

        fun <I> whenTrue(doOptionally: SQLAction<I, I>, whenTrue: (I) -> Boolean): SQLAction<I, I> =
                WhenTrue(doOptionally, map<I, Boolean>(whenTrue))

        fun <I, J, O> whenRight(doOptionally: SQLAction<J, O>, whenRight: SQLAction<I, Either<O, J>>): SQLAction<I, O> =
                WhenRight(doOptionally, whenRight)

        fun <I, J> whenNonNull(doOptionally: SQLAction<J, I>, whenNonNull: SQLAction<I, J?>): SQLAction<I, I> =
                WhenNonNull(doOptionally, whenNonNull)

        fun <I, J> whenTrue(doOptionally: SQLAction<I, I>, whenTrue: SQLAction<I, Boolean>): SQLAction<I, I> =
                WhenTrue(doOptionally, whenTrue)
    }

    fun <U> doWithConnection(action: (O, SQLConnection) -> Future<U>): SQLAction<I, U> =
            andThen(DoWithConnection(action))

    fun <K> query(toSql: (O) -> SQLStatement, mapResult: (O, ResultSet) -> Try<K>): SQLAction<I, K> =
            query(QuerySQLMapping.create(toSql, mapResult))

    fun <K> query(mapping: QuerySQLMapping<O, K>): SQLAction<I, K> =
            andThen(Query(mapping))

    fun <K> update(toSql: (O) -> SQLStatement, mapResult: (O, UpdateResult) -> Try<K>): SQLAction<I, K> =
            update(UpdateSQLMapping.create(toSql, mapResult))

    fun <K> update(mapping: UpdateSQLMapping<O, K>): SQLAction<I, K> =
            andThen(Update(mapping))

    fun exec(statment: String): SQLAction<I, O> =
            andThen(Exec(statment))

    fun dropTable(tableName: String): SQLAction<I, O> =
            exec("DROP TABLE $tableName")

    fun dropTableIfExists(tableName: String): SQLAction<I, O> =
            exec("DROP TABLE IF EXISTS $tableName")

    fun deleteAll(tableName: (O) -> String): SQLAction<I, O> =
            update({ o -> SQLStatement.Simple("DELETE FROM ${tableName(o)}") }, { a, _ -> Try.Success(a) })

    fun <K> mapTask(task: Task<O, K>): SQLAction<I, K> =
            andThen(MapTask(task))

    fun <K> map(mapper: (O) -> K): SQLAction<I, K> =
            andThen(Map(mapper))

    fun <K> mapTry(mapper: (O) -> Try<K>): SQLAction<I, K> =
            andThen(MapTry(mapper))

    fun <K> flatMap(next: SQLAction<O, K>): SQLAction<I, K> =
            andThen(next)

    fun <J, K> whenRight(doOptionally: SQLAction<J, K>, whenRight: (O) -> Either<K, J>): SQLAction<I, K> =
            andThen(WhenRight(doOptionally, map<O, Either<K, J>>(whenRight)))

    fun <J> whenNonNull(doOptionally: SQLAction<J, O>, whenNonNull: (O) -> J?): SQLAction<I, O> =
            andThen(WhenNonNull(doOptionally, map<O, J?>(whenNonNull)))

    fun whenTrue(doOptionally: SQLAction<O, O>, whenTrue: (O) -> Boolean): SQLAction<I, O> =
            andThen(WhenTrue(doOptionally, map<O, Boolean>(whenTrue)))

    fun <J, K> whenRight(doOptionally: SQLAction<J, K>, whenRight: SQLAction<O, Either<K, J>>): SQLAction<I, K> =
            andThen(WhenRight(doOptionally, whenRight))

    fun <J> whenNonNull(doOptionally: SQLAction<J, O>, whenNonNull: SQLAction<O, J?>): SQLAction<I, O> =
            andThen(WhenNonNull(doOptionally, whenNonNull))

    fun whenTrue(doOptionally: SQLAction<O, O>, whenTrue: SQLAction<O, Boolean>): SQLAction<I, O> =
            andThen(WhenTrue(doOptionally, whenTrue))
    
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