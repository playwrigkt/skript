package dev.yn.playground.sql.transaction

import dev.yn.playground.Skript
import dev.yn.playground.context.SQLSkriptContext
import dev.yn.playground.result.AsyncResult


sealed class SQLTransactionSkript<I, O, C: SQLSkriptContext<*>>: Skript<I, O, C> {
    abstract fun <J> mapInsideTransaction(skript: Skript<O, J, C>): SQLTransactionSkript<I, J, C>

    abstract val transaction: Skript<I, O, C>
    companion object {
        fun <I, O, C: SQLSkriptContext<*>> transaction(skript: Skript<I, O, C>): SQLTransactionSkript<I, O, C> =
                when(skript) {
                    is SQLTransactionSkript -> transaction(skript.transaction)
                    else -> TransactionalSQLTransactionSkript(skript)
                }

        fun <I, O, C: SQLSkriptContext<*>> autoCommit(skript: Skript<I, O, C>): SQLTransactionSkript<I, O, C> =
                when(skript) {
                    is SQLTransactionSkript -> autoCommit(skript.transaction)
                    else -> AutoCommitSQlTransactionSkript(skript)
                }
    }

    data class AutoCommitSQlTransactionSkript<I, O, C: SQLSkriptContext<*>>(override val transaction: Skript<I, O, C>) : SQLTransactionSkript<I, O, C>() {

        override fun <J> mapInsideTransaction(skript: Skript<O, J, C>): SQLTransactionSkript<I, J, C> =
                when(skript) {
                    is SQLTransactionSkript -> this.mapInsideTransaction(skript.transaction)
                    else -> AutoCommitSQlTransactionSkript(this.transaction.flatMap(skript))

                }

        override fun run(i: I, context: C): AsyncResult<O> =
                context.getSQLExecutor().setAutoCommit(true)
                        .flatMap { transaction.run(i, context) }
                        .flatMap(context.getSQLExecutor().close())
                        .recover(context.getSQLExecutor().closeOnFailure())
    }

    data class TransactionalSQLTransactionSkript<I, O, C: SQLSkriptContext<*>>(override val transaction: Skript<I, O, C>) : SQLTransactionSkript<I, O, C>() {
        override fun <J> mapInsideTransaction(skript: Skript<O, J, C>): SQLTransactionSkript<I, J, C> =
                when(skript) {
                    is SQLTransactionSkript -> this.mapInsideTransaction(skript.transaction)
                    else -> TransactionalSQLTransactionSkript(this.transaction.flatMap(skript))
                }

        override fun run(i: I, context: C): AsyncResult<O> =
                context.getSQLExecutor().setAutoCommit(false)
                        .flatMap {
                            transaction.run(i, context)
                                    .flatMap(context.getSQLExecutor().commit())
                                    .recover(context.getSQLExecutor().rollback())
                        }
                        .flatMap(context.getSQLExecutor().close())
                        .recover(context.getSQLExecutor().closeOnFailure())
    }
}

