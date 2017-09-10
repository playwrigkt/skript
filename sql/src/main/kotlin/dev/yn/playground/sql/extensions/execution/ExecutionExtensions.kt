package dev.yn.playground.sql.extensions.execution

import dev.yn.playground.sql.SQLTransaction
import dev.yn.playground.sql.SQLTransactionExecutor
import io.vertx.core.Future

fun execute(executor: SQLTransactionExecutor, transaction: SQLTransaction<Unit, Unit, Unit>): Future<Unit> {
    return executor.execute(transaction)
}

fun <I> Future<I>.execute(executor: SQLTransactionExecutor, transaction: SQLTransaction<Unit, Unit, Unit>): Future<Unit> {
    return this.compose { executor.execute(transaction) }
}

fun <I, J, O> query(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, J, O>): (I) -> Future<O> {
    return { executor.query(it, transaction) }
}

fun <I, J, O> query(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, J, O>, i: I): Future<O> {
    return executor.query(i, transaction)
}

fun <I, J, O> Future<I>.query(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, J, O>): Future<O> {
    return this.compose { executor.query(it, transaction) }
}

fun <I, J, O> update(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, J, O>): (I) -> Future<O> {
    return { executor.update(it, transaction) }
}

fun <I, J, O> update(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, J, O>, i: I): Future<O> {
    return executor.update(i, transaction)
}

fun<I, J, O> Future<I>.update(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, J, O>): Future<O> {
    return this.compose { executor.update(it, transaction) }
}