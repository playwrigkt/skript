package playwrigkt.skript.sql.transaction

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SqlTroupe


sealed class SqlTransactionSkript<I, O, Troupe: SqlTroupe>: Skript<I, O, Troupe> {
    abstract fun <J> mapInsideTransaction(skript: Skript<O, J, Troupe>): SqlTransactionSkript<I, J, Troupe>

    abstract val transaction: Skript<I, O, Troupe>
    companion object {
        fun <I, O, Troupe: SqlTroupe> transaction(skript: Skript<I, O, Troupe>): SqlTransactionSkript<I, O, Troupe> =
                when(skript) {
                    is SqlTransactionSkript -> transaction(skript.transaction)
                    else -> TransactionalSqlTransactionSkript(skript)
                }

        fun <I, O, Troupe: SqlTroupe> autoCommit(skript: Skript<I, O, Troupe>): SqlTransactionSkript<I, O, Troupe> =
                when(skript) {
                    is SqlTransactionSkript -> autoCommit(skript.transaction)
                    else -> AutoCommitSQlTransactionSkript(skript)
                }
    }

    data class AutoCommitSQlTransactionSkript<I, O, Troupe: SqlTroupe>(override val transaction: Skript<I, O, Troupe>) : SqlTransactionSkript<I, O, Troupe>() {

        override fun <J> mapInsideTransaction(skript: Skript<O, J, Troupe>): SqlTransactionSkript<I, J, Troupe> =
                when(skript) {
                    is SqlTransactionSkript -> this.mapInsideTransaction(skript.transaction)
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

    data class TransactionalSqlTransactionSkript<I, O, Troupe: SqlTroupe>(override val transaction: Skript<I, O, Troupe>) : SqlTransactionSkript<I, O, Troupe>() {
        override fun <J> mapInsideTransaction(skript: Skript<O, J, Troupe>): SqlTransactionSkript<I, J, Troupe> =
                when(skript) {
                    is SqlTransactionSkript -> this.mapInsideTransaction(skript.transaction)
                    else -> TransactionalSqlTransactionSkript(this.transaction.compose(skript))
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

