package dev.yn.playground.sql

import io.vertx.core.Future
import io.vertx.ext.sql.SQLConnection

/**
 * A re-usable SQL actionChain.  Executed via SQLTask
 *
 * A SQLTranaction is a chain of `SQLAction`
 *
 * I input Type
 * O output type
 */
sealed class SQLActionChain<I, O> {
    abstract fun <U> addAction(action: SQLAction<O, U>): SQLActionChain<I, U>
    abstract fun run(i: I, connection: SQLConnection): Future<O>

    internal data class EndLink<I, O>(val action: SQLAction<I, O>): SQLActionChain<I, O>() {
        override fun <U> addAction(action: SQLAction<O, U>): SQLActionChain<I, U> =
                ActionLink(this.action, EndLink(action))

        override fun run(i: I, connection: SQLConnection): Future<O> =
                action.run(i, connection)

        override fun toString(): String = "SQLActionChain.EndLink(head=$action)"
    }

    internal data class ActionLink<I, J, O>(val action: SQLAction<I, J>,
                                      val next: SQLActionChain<J, O>): SQLActionChain<I, O>() {
        override fun <U> addAction(action: SQLAction<O, U>): SQLActionChain<I, U> =
                ActionLink(this.action, next.addAction(action))

        override fun run(i: I, connection: SQLConnection): Future<O> =
                action.run(i, connection)
                        .compose { u -> next.run(u, connection) }

        override fun toString(): String = "SQLActionChain.ActionLink(head=$action,tail=$next)"
    }
}