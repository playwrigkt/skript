package devyn.playground.sql.task

import dev.yn.playground.sql.context.SQLTaskContext
import dev.yn.playground.task.Task
import dev.yn.playground.task.result.AsyncResult


sealed class SQLTransactionTask<I, O, C: SQLTaskContext<*>>: Task<I, O, C> {
    abstract fun <J> mapInsideTransaction(task: Task<O, J, C>): SQLTransactionTask<I, J, C>

    abstract val transaction: Task<I, O, C>
    companion object {
        fun <I, O, C: SQLTaskContext<*>> transaction(task: Task<I, O, C>): SQLTransactionTask<I, O, C> =
                when(task) {
                    is SQLTransactionTask -> transaction(task.transaction)
                    else -> TransactionalSQLTransactionTask(task)
                }

        fun <I, O, C: SQLTaskContext<*>> autoCommit(task: Task<I, O, C>): SQLTransactionTask<I, O, C> =
                when(task) {
                    is SQLTransactionTask -> Companion.autoCommit(task.transaction)
                    else -> AutoCommitSQlTransactionTask(task)
                }
    }

    data class AutoCommitSQlTransactionTask<I, O, C: SQLTaskContext<*>>(override val transaction: Task<I, O, C>) : SQLTransactionTask<I, O, C>() {

        override fun <J> mapInsideTransaction(task: Task<O, J, C>): SQLTransactionTask<I, J, C> =
                when(task) {
                    is SQLTransactionTask -> this.mapInsideTransaction(task.transaction)
                    else -> AutoCommitSQlTransactionTask(this.transaction.andThen(task))

                }

        override fun run(i: I, context: C): AsyncResult<O> =
                context.getSQLActionContext().setAutoCommit(true)
                        .flatMap { transaction.run(i, context) }
                        .flatMap(context.getSQLActionContext().close())
                        .recover(context.getSQLActionContext().closeOnFailure())
    }

    data class TransactionalSQLTransactionTask<I, O, C: SQLTaskContext<*>>(override val transaction: Task<I, O, C>) : SQLTransactionTask<I, O, C>() {
        override fun <J> mapInsideTransaction(task: Task<O, J, C>): SQLTransactionTask<I, J, C> =
                when(task) {
                    is SQLTransactionTask -> this.mapInsideTransaction(task.transaction)
                    else -> TransactionalSQLTransactionTask(this.transaction.andThen(task))
                }

        override fun run(i: I, context: C): AsyncResult<O> =
                context.getSQLActionContext().setAutoCommit(false)
                        .flatMap {
                            transaction.run(i, context)
                                    .flatMap(context.getSQLActionContext().commit())
                                    .recover(context.getSQLActionContext().rollback())
                        }
                        .flatMap(context.getSQLActionContext().close())
                        .recover(context.getSQLActionContext().closeOnFailure())
    }
}

