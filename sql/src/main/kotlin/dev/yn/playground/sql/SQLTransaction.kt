package dev.yn.playground.sql

import io.vertx.core.Future
import io.vertx.ext.sql.SQLConnection

/**
 * A re-usable SQL transaction.  Use SQLTransactinExecutor to handle transactions autmatically.
 *
 * A SQLTranaction is a chain of `SQLAction`
 *
 * I input Type
 * J result of first transformation
 * O output type
 */
sealed class SQLTransaction<I, J, O> {
    companion object {
        fun <I, O> new(action: SQLAction<I, O>): SQLTransaction<I, O, O> {
            return EndLink(action)
        }
    }

    abstract fun <U> addAction(action: SQLAction<O, U>): SQLTransaction<I, J, U>
    abstract fun run(i: I, connection: SQLConnection): Future<O>

    data class EndLink<I, O>(val action: SQLAction<I, O>): SQLTransaction<I, O, O>() {
        override fun <U> addAction(action: SQLAction<O, U>): SQLTransaction<I, O, U> =
                ActionLink(this.action, EndLink(action))

        override fun run(i: I, connection: SQLConnection): Future<O> =
                action.run(i, connection)
    }

    data class ActionLink<I, J, K, O>(val action: SQLAction<I, J>,
                                      val next: SQLTransaction<J, K, O>): SQLTransaction<I, J, O>() {
        override fun <U> addAction(action: SQLAction<O, U>): SQLTransaction<I, J, U> =
                ActionLink(this.action, next.addAction(action))

        override fun run(i: I, connection: SQLConnection): Future<O> =
                action.run(i, connection)
                        .compose { u -> next.run(u, connection) }
    }
}