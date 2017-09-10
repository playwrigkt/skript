package dev.yn.playground.user.extensions.transaction

import dev.yn.playground.sql.SQLTransactionExecutor
import dev.yn.playground.user.UserTransactions
import io.vertx.core.Future

fun SQLTransactionExecutor.deleteAllUsers() = this.execute(UserTransactions.deleteAllUsersTransaction)
fun <T> Future<T>.deleteAllUsers(executor: SQLTransactionExecutor) = executor.deleteAllUsers()