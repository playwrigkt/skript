package dev.yn.playground.sql

import dev.yn.playground.task.UnpreparedTask
import io.vertx.core.Future
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.UpdateResult
import org.funktionale.tries.Try

sealed class UnpreparedSQLAction<I, O, P> {
    abstract fun prepare(provider: P): SQLAction<I, O>

    class Query<I, O, P>(val mapping: QuerySQLMapping<I, O>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.Query(mapping)
        }

        override fun toString(): String =
                "UnpreparedSQLAction.Query(mapping=$mapping)"
    }

    class Map<I, O, P>(val mapper: (I) -> O): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.Map(mapper)
        }

        override fun toString(): String =
                "UnpreparedSQLAction.Map(mapper:$mapper)"
    }

    class MapTask<I, O, P>(val task: UnpreparedTask<I, O, P>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.MapTask(task.prepare(provider))
        }
    }
    class MapAsync<I, O, P>(val mapper: (I) -> Future<O>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.MapAsync(mapper)
        }

        override fun toString(): String =
                "SQLAction.MapAsync(mapper:$mapper)"
    }

    class Update<I, O, P>(val mapping: UpdateSQLMapping<I, O>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.Update(mapping)
        }

        override fun toString(): String = "SQLAction.Update(mapping=$mapping)"
    }

    class Exec<I, P>(val statement: String): UnpreparedSQLAction<I, I, P>() {
        override fun prepare(provider: P): SQLAction<I, I> {
            return SQLAction.Exec(statement)
        }

        override fun toString(): String = "SQLAction.Exec(statement=$statement)"
    }
    class Nested<I, O, P>(val chain: UnpreparedSQLActionChain<I, O, P>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.Nested(chain.prepare(provider))
        }

        override fun toString(): String = "SQLAction.Nested(chain=$chain)"
    }
}