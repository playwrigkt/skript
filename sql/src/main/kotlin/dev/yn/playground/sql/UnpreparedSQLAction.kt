package dev.yn.playground.sql

import dev.yn.playground.task.UnpreparedTask
import io.vertx.core.Future
import io.vertx.ext.sql.SQLConnection
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
    abstract fun prepare(provider: P): SQLAction<I, O>

    data class Query<I, O, P>(val mapping: QuerySQLMapping<I, O>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.Query(mapping)
        }
    }

    data class Update<I, O, P>(val mapping: UpdateSQLMapping<I, O>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.Update(mapping)
        }
    }

    data class Exec<I, P>(val statement: String): UnpreparedSQLAction<I, I, P>() {
        override fun prepare(provider: P): SQLAction<I, I> {
            return SQLAction.Exec(statement)
        }
    }

    data class Nested<I, O, P>(val chain: UnpreparedSQLActionChain<I, O, P>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.Nested(chain.prepare(provider))
        }
    }


    data class Map<I, O, P>(val mapper: (I) -> O): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.Map(mapper)
        }
    }

    internal data class MapTry<I, O, P>(val mapper: (I) -> Try<O>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.MapTry(mapper)
        }
    }

    data class MapTask<I, O, P>(val task: UnpreparedTask<I, O, P>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.MapTask(task.prepare(provider))
        }
    }

    data class Optional<I, J, O, P>(val doAction: UnpreparedSQLActionChain<J, O, P>, val whenRight: UnpreparedSQLActionChain<I, Either<O, J>, P>): UnpreparedSQLAction<I, O, P>() {
        override fun prepare(provider: P): SQLAction<I, O> {
            return SQLAction.Optional(doAction.prepare(provider), whenRight.prepare(provider))
        }
    }
}