package dev.yn.playground.sql.extensions.future

import dev.yn.playground.sql.SQLTransaction
import dev.yn.playground.sql.SQLTransactionExecutor
import io.vertx.core.Future

fun <I, O> Future<I>.execute(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, O>): Future<O> {
    return this.compose { executor.execute(it, transaction) }
}

fun <I, O> Future<I>.query(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, O>): Future<O> {
    return this.compose { executor.execute(it, transaction) }
}

fun<I, O> Future<I>.update(executor: SQLTransactionExecutor, transaction: SQLTransaction<I, O>): Future<O> {
    return this.compose { executor.executeTransactionally(it, transaction) }
}