package dev.yn.playground.sql

import dev.yn.playground.task.UnpreparedTask
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
 * P the provider type to be used to prepare non SQL tasks
 */
sealed class UnpreparedSQLAction<I, O, P> {
    companion object {
        fun <I, O, P> doWithConnection(action: (I, SQLConnection) -> Future<O>): UnpreparedSQLAction<I, O, P> =
                DoWithConnection(action)

        fun <I, O, P> query(toSql: (I) -> SQLStatement, mapResult: (I, ResultSet) -> Try<O>): UnpreparedSQLAction<I, O, P> =
                query(QuerySQLMapping.create(toSql, mapResult))

        fun <I, O, P> query(mapping: QuerySQLMapping<I, O>): UnpreparedSQLAction<I, O, P> =
                UnpreparedSQLAction.Query(mapping)

        fun <I, O, P> update(toSql: (I) -> SQLStatement, mapResult: (I, UpdateResult) -> Try<O>): UnpreparedSQLAction<I, O, P> =
                update(UpdateSQLMapping.create(toSql, mapResult))

        fun <I, O, P> update(mapping: UpdateSQLMapping<I, O>): UnpreparedSQLAction<I, O, P> =
                UnpreparedSQLAction.Update(mapping)

        fun <I, P> exec(statment: String): UnpreparedSQLAction<I, I, P> =
                UnpreparedSQLAction.Exec(statment)

        fun <I, P> dropTable(tableName: String): UnpreparedSQLAction<I, I, P> =
                exec("DROP TABLE $tableName")

        fun <I, P> dropTableIfExists(tableName: String): UnpreparedSQLAction<I, I, P> =
                exec("DROP TABLE IF EXISTS $tableName")

        fun <I, P> deleteAll(tableName: (I) -> String): UnpreparedSQLAction<I, I, P> =
                update({ i -> SQLStatement.Simple("DELETE FROM ${tableName(i)}") }, { a, _ -> Try.Success(a) })

        fun <I, P> deleteAll(tableName: String): UnpreparedSQLAction<I, I, P> =
                update({ SQLStatement.Simple("DELETE FROM $tableName") }, { a, _ -> Try.Success(a) })

        fun <I, O, P> task(task: UnpreparedTask<I, O, P>): UnpreparedSQLAction<I, O, P> =
                UnpreparedSQLAction.MapTask(task)

        fun <I, O, P> map(mapper: (I) -> O): UnpreparedSQLAction<I, O, P> =
                UnpreparedSQLAction.Map(mapper)

        fun <I, O, P> mapTry(mapper: (I) -> Try<O>): UnpreparedSQLAction<I, O, P> =
                UnpreparedSQLAction.MapTry(mapper)

        fun <I, J, O, P> whenRight(doOptionally: UnpreparedSQLAction<J, O, P>, whenRight: (I) -> Either<O, J>): UnpreparedSQLAction<I, O, P> =
                UnpreparedSQLAction.WhenRight(doOptionally, map<I, Either<O, J>, P>(whenRight))

        fun <I, J, P> whenNonNull(doOptionally: UnpreparedSQLAction<J, I, P>, whenNonNull: (I) -> J?): UnpreparedSQLAction<I, I, P> =
                UnpreparedSQLAction.WhenNonNull(doOptionally, map<I, J?, P>(whenNonNull))

        fun <I, P> whenTrue(doOptionally: UnpreparedSQLAction<I, I, P>, whenTrue: (I) -> Boolean): UnpreparedSQLAction<I, I, P> =
                UnpreparedSQLAction.WhenTrue(doOptionally, map<I, Boolean, P>(whenTrue))

        fun <I, J, O, P> whenRight(doOptionally: UnpreparedSQLAction<J, O, P>, whenRight: UnpreparedSQLAction<I, Either<O, J>, P>): UnpreparedSQLAction<I, O, P> =
                UnpreparedSQLAction.WhenRight(doOptionally, whenRight)

        fun <I, J, P> whenNonNull(doOptionally: UnpreparedSQLAction<J, I, P>, whenNonNull: UnpreparedSQLAction<I, J?, P>): UnpreparedSQLAction<I, I, P> =
                UnpreparedSQLAction.WhenNonNull(doOptionally, whenNonNull)

        fun <I, J, P> whenTrue(doOptionally: UnpreparedSQLAction<I, I, P>, whenTrue: UnpreparedSQLAction<I, Boolean, P>): UnpreparedSQLAction<I, I, P> =
                UnpreparedSQLAction.WhenTrue(doOptionally, whenTrue)
    }

    fun <U> doWithConnection(action: (O, SQLConnection) -> Future<U>): UnpreparedSQLAction<I, U, P> =
            addAction(DoWithConnection(action))

    fun <K> query(toSql: (O) -> SQLStatement, mapResult: (O, ResultSet) -> Try<K>): UnpreparedSQLAction<I, K, P> =
            query(QuerySQLMapping.create(toSql, mapResult))

    fun <K> query(mapping: QuerySQLMapping<O, K>): UnpreparedSQLAction<I, K, P> =
            addAction(UnpreparedSQLAction.Query(mapping))

    fun <K> update(toSql: (O) -> SQLStatement, mapResult: (O, UpdateResult) -> Try<K>): UnpreparedSQLAction<I, K, P> =
            update(UpdateSQLMapping.create(toSql, mapResult))

    fun <K> update(mapping: UpdateSQLMapping<O, K>): UnpreparedSQLAction<I, K, P> =
            addAction(UnpreparedSQLAction.Update(mapping))

    fun exec(statment: String): UnpreparedSQLAction<I, O, P> =
            addAction(UnpreparedSQLAction.Exec(statment))

    fun dropTable(tableName: String): UnpreparedSQLAction<I, O, P> =
            exec("DROP TABLE $tableName")

    fun dropTableIfExists(tableName: String): UnpreparedSQLAction<I, O, P> =
            exec("DROP TABLE IF EXISTS $tableName")

    fun deleteAll(tableName: (O) -> String): UnpreparedSQLAction<I, O, P> =
            update({ o -> SQLStatement.Simple("DELETE FROM ${tableName(o)}") }, { a, _ -> Try.Success(a) })

    fun <K> mapTask(task: UnpreparedTask<O, K, P>): UnpreparedSQLAction<I, K, P> =
            addAction(UnpreparedSQLAction.MapTask(task))

    fun <K> map(mapper: (O) -> K): UnpreparedSQLAction<I, K, P> =
            addAction(UnpreparedSQLAction.Map(mapper))

    fun <K> mapTry(mapper: (O) -> Try<K>): UnpreparedSQLAction<I, K, P> =
            addAction(UnpreparedSQLAction.MapTry(mapper))

    fun <K> flatMap(next: UnpreparedSQLAction<O, K, P>): UnpreparedSQLAction<I, K, P> =
            addAction(next)

    fun <J, K> whenRight(doOptionally: UnpreparedSQLAction<J, K, P>, whenRight: (O) -> Either<K, J>): UnpreparedSQLAction<I, K, P> =
            addAction(UnpreparedSQLAction.WhenRight(doOptionally, map<O, Either<K, J>, P>(whenRight)))

    fun <J> whenNonNull(doOptionally: UnpreparedSQLAction<J, O, P>, whenNonNull: (O) -> J?): UnpreparedSQLAction<I, O, P> =
            addAction(UnpreparedSQLAction.WhenNonNull(doOptionally, map<O, J?, P>(whenNonNull)))

    fun whenTrue(doOptionally: UnpreparedSQLAction<O, O, P>, whenTrue: (O) -> Boolean): UnpreparedSQLAction<I, O, P> =
            addAction(UnpreparedSQLAction.WhenTrue(doOptionally, map<O, Boolean, P>(whenTrue)))

    fun <J, K> whenRight(doOptionally: UnpreparedSQLAction<J, K, P>, whenRight: UnpreparedSQLAction<O, Either<K, J>, P>): UnpreparedSQLAction<I, K, P> =
            addAction(UnpreparedSQLAction.WhenRight(doOptionally, whenRight))

    fun <J> whenNonNull(doOptionally: UnpreparedSQLAction<J, O, P>, whenNonNull: UnpreparedSQLAction<O, J?, P>): UnpreparedSQLAction<I, O, P> =
            addAction(UnpreparedSQLAction.WhenNonNull(doOptionally, whenNonNull))

    fun whenTrue(doOptionally: UnpreparedSQLAction<O, O, P>, whenTrue: UnpreparedSQLAction<O, Boolean, P>): UnpreparedSQLAction<I, O, P> =
            addAction(UnpreparedSQLAction.WhenTrue(doOptionally, whenTrue))

    abstract fun prepare(provider: P): SQLAction<I, O>

    open fun <U> addAction(action: UnpreparedSQLAction<O, U, P>): UnpreparedSQLAction<I, U, P> {
        return Link(this, action)
    }

    internal data class DoWithConnection<I, O, P>(val action: (I, SQLConnection) -> Future<O>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.DoWithConnection(action)
        }
    }
    internal data class Link<I, J, O, P>(val head: UnpreparedSQLAction<I, J, P>, val tail: UnpreparedSQLAction<J, O, P>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.Link(head.prepare(provider), tail.prepare(provider))
        }
        
        override fun <U> addAction(action: UnpreparedSQLAction<O, U, P>): UnpreparedSQLAction<I, U, P> {
            return Link(this.head, this.tail.addAction(action))
        }
    }

    internal data class Query<I, O, P>(val mapping: QuerySQLMapping<I, O>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.Query(mapping)
        }
    }

    internal data class Update<I, O, P>(val mapping: UpdateSQLMapping<I, O>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.Update(mapping)
        }
    }

    internal data class Exec<I, P>(val statement: String): UnpreparedSQLAction<I, I, P>() {
        override fun prepare(provider: P): SQLAction<I, I> {
            return SQLAction.Exec(statement)
        }
    }


    internal data class Map<I, O, P>(val mapper: (I) -> O): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.Map(mapper)
        }
    }

    internal data class MapTry<I, O, P>(val mapper: (I) -> Try<O>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.MapTry(mapper)
        }
    }

    internal data class MapTask<I, O, P>(val task: UnpreparedTask<I, O, P>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.MapTask(task.prepare(provider))
        }
    }

    internal data class WhenRight<I, J, O, P>(val doOptionally: UnpreparedSQLAction<J, O, P>, val whenRight: UnpreparedSQLAction<I, Either<O, J>, P>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.WhenRight(doOptionally.prepare(provider), whenRight.prepare(provider))
        }
    }

    internal data class WhenNonNull<I, J, P>(val doOptionally: UnpreparedSQLAction<J, I, P>, val whenNonNull: UnpreparedSQLAction<I, J?, P>): UnpreparedSQLAction<I, I, P>() {
        override fun prepare(provider: P): SQLAction<I, I> {
            return SQLAction.WhenNonNull(doOptionally.prepare(provider), whenNonNull.prepare(provider))
        }
    }

    internal data class WhenTrue<I, P>(val doOptionally: UnpreparedSQLAction<I, I, P>, val whenTrue: UnpreparedSQLAction<I, Boolean, P>): UnpreparedSQLAction<I, I, P>() {
        override fun prepare(provider: P): SQLAction<I, I> {
            return SQLAction.WhenTrue(doOptionally.prepare(provider), whenTrue.prepare(provider))
        }
    }
}