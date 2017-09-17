package dev.yn.playground.sql

import dev.yn.playground.task.UnpreparedTask
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.UpdateResult
import org.funktionale.tries.Try

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
    }
    
    abstract fun <U> addAction(action: UnpreparedSQLAction<O, U, P>): UnpreparedSQLActionChain<I, U, P>
    abstract fun prepare(provider: P): SQLActionChain<I, O>

    data class EndLink<I, O, P>(val action: UnpreparedSQLAction<I, O, P>): UnpreparedSQLActionChain<I, O, P>() {
        override fun prepare(provider: P): SQLActionChain<I, O> {
            return SQLActionChain.EndLink(action.prepare(provider))
        }

        override fun <U> addAction(action: UnpreparedSQLAction<O, U, P>): UnpreparedSQLActionChain<I, U, P> {
            return ActionLink(this.action, EndLink(action))
        }
    }

    data class ActionLink<I, J, O, P>(val action: UnpreparedSQLAction<I, J, P>, val next: UnpreparedSQLActionChain<J, O, P>): UnpreparedSQLActionChain<I, O, P>() {
        override fun <U> addAction(action: UnpreparedSQLAction<O, U, P>): UnpreparedSQLActionChain<I, U, P> {
            return ActionLink<I, J, U, P>(this.action, next.addAction(action))
        }

        override fun prepare(provider: P): SQLActionChain<I, O> {
            return SQLActionChain.ActionLink(action.prepare(provider), next.prepare(provider))
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

    fun <K> flatMap(next: UnpreparedSQLActionChain<O, K, P>): UnpreparedSQLActionChain<I, K, P> =
            addAction(UnpreparedSQLAction.Nested(next))
}