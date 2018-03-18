package playwright.skript.sql.transaction

import playwright.skript.Skript
import playwright.skript.result.AsyncResult
import playwright.skript.stage.SQLStage


sealed class SQLTransactionSkript<I, O, Stage: SQLStage>: Skript<I, O, Stage> {
    abstract fun <J> mapInsideTransaction(skript: Skript<O, J, Stage>): SQLTransactionSkript<I, J, Stage>

    abstract val transaction: Skript<I, O, Stage>
    companion object {
        fun <I, O, Stage: SQLStage> transaction(skript: Skript<I, O, Stage>): SQLTransactionSkript<I, O, Stage> =
                when(skript) {
                    is SQLTransactionSkript -> transaction(skript.transaction)
                    else -> TransactionalSQLTransactionSkript(skript)
                }

        fun <I, O, Stage: SQLStage> autoCommit(skript: Skript<I, O, Stage>): SQLTransactionSkript<I, O, Stage> =
                when(skript) {
                    is SQLTransactionSkript -> autoCommit(skript.transaction)
                    else -> AutoCommitSQlTransactionSkript(skript)
                }
    }

    data class AutoCommitSQlTransactionSkript<I, O, Stage: SQLStage>(override val transaction: Skript<I, O, Stage>) : SQLTransactionSkript<I, O, Stage>() {

        override fun <J> mapInsideTransaction(skript: Skript<O, J, Stage>): SQLTransactionSkript<I, J, Stage> =
                when(skript) {
                    is SQLTransactionSkript -> this.mapInsideTransaction(skript.transaction)
                    else -> AutoCommitSQlTransactionSkript(this.transaction.flatMap(skript))

                }

        override fun run(i: I, stage: Stage): AsyncResult<O> =
                stage.getSQLPerformer().setAutoCommit(true)
                        .flatMap { transaction.run(i, stage) }
                        .flatMap(stage.getSQLPerformer().close())
                        .recover(stage.getSQLPerformer().closeOnFailure())
    }

    data class TransactionalSQLTransactionSkript<I, O, Stage: SQLStage>(override val transaction: Skript<I, O, Stage>) : SQLTransactionSkript<I, O, Stage>() {
        override fun <J> mapInsideTransaction(skript: Skript<O, J, Stage>): SQLTransactionSkript<I, J, Stage> =
                when(skript) {
                    is SQLTransactionSkript -> this.mapInsideTransaction(skript.transaction)
                    else -> TransactionalSQLTransactionSkript(this.transaction.flatMap(skript))
                }

        override fun run(i: I, stage: Stage): AsyncResult<O> =
                stage.getSQLPerformer().setAutoCommit(false)
                        .flatMap {
                            transaction.run(i, stage)
                                    .flatMap(stage.getSQLPerformer().commit())
                                    .recover(stage.getSQLPerformer().rollback())
                        }
                        .flatMap(stage.getSQLPerformer().close())
                        .recover(stage.getSQLPerformer().closeOnFailure())
    }
}

