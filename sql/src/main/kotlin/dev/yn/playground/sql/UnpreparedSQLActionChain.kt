package dev.yn.playground.sql

import dev.yn.playground.task.UnpreparedTask
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.UpdateResult
import org.funktionale.either.Either
import org.funktionale.tries.Try

/**
 * Providese interface for constructing SQL Transactions
 */
sealed class UnpreparedSQLActionChain<I, O, P> {
    companion object {
        fun <I, O, P> new(action: UnpreparedSQLAction<I, O, P>): UnpreparedSQLActionChain<I, O, P> {
            return EndLink(action)
        }

        fun <I, O, P> query(toSql: (I) -> SQLStatement, mapResult: (I, ResultSet) -> Try<O>): UnpreparedSQLActionChain<I, O, P> =
                query(QuerySQLMapping.create(toSql, mapResult))

        fun <I, O, P> query(mapping: QuerySQLMapping<I, O>): UnpreparedSQLActionChain<I, O, P> =
                new(UnpreparedSQLAction.Query(mapping))

        fun <I, O, P> update(toSql: (I) -> SQLStatement, mapResult: (I, UpdateResult) -> Try<O>): UnpreparedSQLActionChain<I, O, P> =
                update(UpdateSQLMapping.create(toSql, mapResult))

        fun <I, O, P> update(mapping: UpdateSQLMapping<I, O>): UnpreparedSQLActionChain<I, O, P> =
                new(UnpreparedSQLAction.Update(mapping))

        fun <I, P> exec(statment: String): UnpreparedSQLActionChain<I, I, P> =
                new(UnpreparedSQLAction.Exec(statment))

        fun <I, P> dropTable(tableName: String): UnpreparedSQLActionChain<I, I, P> =
                exec("DROP TABLE $tableName")

        fun <I, P> dropTableIfExists(tableName: String): UnpreparedSQLActionChain<I, I, P> =
                exec("DROP TABLE IF EXISTS $tableName")

        fun <I, P> deleteAll(tableName: (I) -> String): UnpreparedSQLActionChain<I, I, P> =
                update({ i -> SQLStatement.Simple("DELETE FROM ${tableName(i)}") }, { a, _ -> Try.Success(a) })

        fun <I, P> deleteAll(tableName: String): UnpreparedSQLActionChain<I, I, P> =
                update({ SQLStatement.Simple("DELETE FROM $tableName") }, { a, _ -> Try.Success(a) })

        fun <I, O, P> task(task: UnpreparedTask<I, O, P>): UnpreparedSQLActionChain<I, O, P> =
                new(UnpreparedSQLAction.MapTask(task))

        fun <I, O, P> map(mapper: (I) -> O): UnpreparedSQLActionChain<I, O, P> =
                new(UnpreparedSQLAction.Map(mapper))

        fun <I, O, P> mapTry(mapper: (I) -> Try<O>): UnpreparedSQLActionChain<I, O, P> =
                new(UnpreparedSQLAction.MapTry(mapper))

        fun <I, J, O, P> whenRight(doOptionally: UnpreparedSQLActionChain<J, O, P>, whenRight: (I) -> Either<O, J>): UnpreparedSQLActionChain<I, O, P> =
                new(UnpreparedSQLAction.WhenRight(doOptionally, map<I, Either<O, J>, P>(whenRight)))

        fun <I, J, P> whenNonNull(doOptionally: UnpreparedSQLActionChain<J, I, P>, whenNonNull: (I) -> J?): UnpreparedSQLActionChain<I, I, P> =
                new(UnpreparedSQLAction.WhenNonNull(doOptionally, map<I, J?, P>(whenNonNull)))

        fun <I, P> whenTrue(doOptionally: UnpreparedSQLActionChain<I, I, P>, whenTrue: (I) -> Boolean): UnpreparedSQLActionChain<I, I, P> =
                new(UnpreparedSQLAction.WhenTrue(doOptionally, map<I, Boolean, P>(whenTrue)))

        fun <I, J, O, P> whenRight(doOptionally: UnpreparedSQLActionChain<J, O, P>, whenRight: UnpreparedSQLActionChain<I, Either<O, J>, P>): UnpreparedSQLActionChain<I, O, P> =
                new(UnpreparedSQLAction.WhenRight(doOptionally, whenRight))

        fun <I, J, P> whenNonNull(doOptionally: UnpreparedSQLActionChain<J, I, P>, whenNonNull: UnpreparedSQLActionChain<I, J?, P>): UnpreparedSQLActionChain<I, I, P> =
                new(UnpreparedSQLAction.WhenNonNull(doOptionally, whenNonNull))

        fun <I, J, P> whenTrue(doOptionally: UnpreparedSQLActionChain<I, I, P>, whenTrue: UnpreparedSQLActionChain<I, Boolean, P>): UnpreparedSQLActionChain<I, I, P> =
                new(UnpreparedSQLAction.WhenTrue(doOptionally, whenTrue))

    }
    
    abstract fun <U> addAction(action: UnpreparedSQLAction<O, U, P>): UnpreparedSQLActionChain<I, U, P>
    abstract fun prepare(provider: P): SQLActionChain<I, O>

    internal data class EndLink<I, O, P>(val head: UnpreparedSQLAction<I, O, P>): UnpreparedSQLActionChain<I, O, P>() {
        override fun prepare(provider: P): SQLActionChain<I, O> {
            return SQLActionChain.EndLink(head.prepare(provider))
        }

        override fun <U> addAction(action: UnpreparedSQLAction<O, U, P>): UnpreparedSQLActionChain<I, U, P> {
            return ActionLink(this.head, EndLink(action))
        }
    }

    internal data class ActionLink<I, J, O, P>(val head: UnpreparedSQLAction<I, J, P>, val tail: UnpreparedSQLActionChain<J, O, P>): UnpreparedSQLActionChain<I, O, P>() {
        override fun <U> addAction(action: UnpreparedSQLAction<O, U, P>): UnpreparedSQLActionChain<I, U, P> {
            return ActionLink<I, J, U, P>(this.head, tail.addAction(action))
        }

        override fun prepare(provider: P): SQLActionChain<I, O> {
            return SQLActionChain.ActionLink(head.prepare(provider), tail.prepare(provider))
        }
    }

    fun <K> query(toSql: (O) -> SQLStatement, mapResult: (O, ResultSet) -> Try<K>): UnpreparedSQLActionChain<I, K, P> =
            query(QuerySQLMapping.create(toSql, mapResult))

    fun <K> query(mapping: QuerySQLMapping<O, K>): UnpreparedSQLActionChain<I, K, P> =
            addAction(UnpreparedSQLAction.Query(mapping))

    fun <K> update(toSql: (O) -> SQLStatement, mapResult: (O, UpdateResult) -> Try<K>): UnpreparedSQLActionChain<I, K, P> =
            update(UpdateSQLMapping.create(toSql, mapResult))

    fun <K> update(mapping: UpdateSQLMapping<O, K>): UnpreparedSQLActionChain<I, K, P> =
            addAction(UnpreparedSQLAction.Update(mapping))

    fun exec(statment: String): UnpreparedSQLActionChain<I, O, P> =
            addAction(UnpreparedSQLAction.Exec(statment))

    fun dropTable(tableName: String): UnpreparedSQLActionChain<I, O, P> =
            exec("DROP TABLE $tableName")

    fun dropTableIfExists(tableName: String): UnpreparedSQLActionChain<I, O, P> =
            exec("DROP TABLE IF EXISTS $tableName")

    fun deleteAll(tableName: (O) -> String): UnpreparedSQLActionChain<I, O, P> =
            update({ o -> SQLStatement.Simple("DELETE FROM ${tableName(o)}") }, { a, _ -> Try.Success(a) })

    fun <K> mapTask(task: UnpreparedTask<O, K, P>): UnpreparedSQLActionChain<I, K, P> =
            addAction(UnpreparedSQLAction.MapTask(task))

    fun <K> map(mapper: (O) -> K): UnpreparedSQLActionChain<I, K, P> =
            addAction(UnpreparedSQLAction.Map(mapper))

    fun <K> mapTry(mapper: (O) -> Try<K>): UnpreparedSQLActionChain<I, K, P> =
            addAction(UnpreparedSQLAction.MapTry(mapper))

    fun <K> flatMap(next: UnpreparedSQLActionChain<O, K, P>): UnpreparedSQLActionChain<I, K, P> =
            addAction(UnpreparedSQLAction.Nested(next))

    fun <J, K> whenRight(doOptionally: UnpreparedSQLActionChain<J, K, P>, whenRight: (O) -> Either<K, J>): UnpreparedSQLActionChain<I, K, P> =
            addAction(UnpreparedSQLAction.WhenRight(doOptionally, map<O, Either<K, J>, P>(whenRight)))

    fun <J> whenNonNull(doOptionally: UnpreparedSQLActionChain<J, O, P>, whenNonNull: (O) -> J?): UnpreparedSQLActionChain<I, O, P> =
            addAction(UnpreparedSQLAction.WhenNonNull(doOptionally, map<O, J?, P>(whenNonNull)))

    fun whenTrue(doOptionally: UnpreparedSQLActionChain<O, O, P>, whenTrue: (O) -> Boolean): UnpreparedSQLActionChain<I, O, P> =
            addAction(UnpreparedSQLAction.WhenTrue(doOptionally, map<O, Boolean, P>(whenTrue)))

    fun <J, K> whenRight(doOptionally: UnpreparedSQLActionChain<J, K, P>, whenRight: UnpreparedSQLActionChain<O, Either<K, J>, P>): UnpreparedSQLActionChain<I, K, P> =
            addAction(UnpreparedSQLAction.WhenRight(doOptionally, whenRight))

    fun <J> whenNonNull(doOptionally: UnpreparedSQLActionChain<J, O, P>, whenNonNull: UnpreparedSQLActionChain<O, J?, P>): UnpreparedSQLActionChain<I, O, P> =
            addAction(UnpreparedSQLAction.WhenNonNull(doOptionally, whenNonNull))

    fun whenTrue(doOptionally: UnpreparedSQLActionChain<O, O, P>, whenTrue: UnpreparedSQLActionChain<O, Boolean, P>): UnpreparedSQLActionChain<I, O, P> =
            addAction(UnpreparedSQLAction.WhenTrue(doOptionally, whenTrue))
}