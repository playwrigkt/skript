package playwrigkt.skript.sql.transaction

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SQLTroupe


sealed class SQLTransactionSkript<I, O, Troupe: SQLTroupe>: Skript<I, O, Troupe> {
    abstract fun <J> mapInsideTransaction(skript: Skript<O, J, Troupe>): SQLTransactionSkript<I, J, Troupe>

    abstract val transaction: Skript<I, O, Troupe>
    companion object {
        fun <I, O, Troupe: SQLTroupe> transaction(skript: Skript<I, O, Troupe>): SQLTransactionSkript<I, O, Troupe> =
                when(skript) {
                    is SQLTransactionSkript -> transaction(skript.transaction)
                    else -> TransactionalSQLTransactionSkript(skript)
                }

        fun <I, O, Troupe: SQLTroupe> autoCommit(skript: Skript<I, O, Troupe>): SQLTransactionSkript<I, O, Troupe> =
                when(skript) {
                    is SQLTransactionSkript -> autoCommit(skript.transaction)
                    else -> AutoCommitSQlTransactionSkript(skript)
                }
    }

    data class AutoCommitSQlTransactionSkript<I, O, Troupe: SQLTroupe>(override val transaction: Skript<I, O, Troupe>) : SQLTransactionSkript<I, O, Troupe>() {

        override fun <J> mapInsideTransaction(skript: Skript<O, J, Troupe>): SQLTransactionSkript<I, J, Troupe> =
                when(skript) {
                    is SQLTransactionSkript -> this.mapInsideTransaction(skript.transaction)
                    else -> AutoCommitSQlTransactionSkript(this.transaction.compose(skript))

                }

        override fun run(i: I, troupe: Troupe): AsyncResult<O> =
                troupe.getSQLPerformer().flatMap { sqlPerformer ->
                    sqlPerformer.setAutoCommit(true)
                            .flatMap { transaction.run(i, troupe) }
                            .flatMap(sqlPerformer.close())
                            .recover(sqlPerformer.closeOnFailure())
                }
    }

    data class TransactionalSQLTransactionSkript<I, O, Troupe: SQLTroupe>(override val transaction: Skript<I, O, Troupe>) : SQLTransactionSkript<I, O, Troupe>() {
        override fun <J> mapInsideTransaction(skript: Skript<O, J, Troupe>): SQLTransactionSkript<I, J, Troupe> =
                when(skript) {
                    is SQLTransactionSkript -> this.mapInsideTransaction(skript.transaction)
                    else -> TransactionalSQLTransactionSkript(this.transaction.compose(skript))
                }

        override fun run(i: I, troupe: Troupe): AsyncResult<O> =
                troupe.getSQLPerformer().flatMap { sqlPerformer ->
                    sqlPerformer.setAutoCommit(false)
                            .flatMap {
                                transaction.run(i, troupe)
                                        .flatMap(sqlPerformer.commit())
                                        .recover(sqlPerformer.rollback())
                            }
                            .flatMap(sqlPerformer.close())
                            .recover(sqlPerformer.closeOnFailure())
                }
    }
}

