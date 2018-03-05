package dev.yn.playground.user.extensions.transaction

import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.sql.context.SQLTaskContext
import dev.yn.playground.user.sql.UserTransactions
import devyn.playground.sql.task.SQLTransactionTask
import io.vertx.core.Future

fun ApplicationContext.deleteAllUsers() = SQLTransactionTask.autoCommit<Unit, Unit, ApplicationContext>(UserTransactions.deleteAllUserActionChain()).run(Unit, this)
fun <T> Future<T>.deleteAllUsers(context: ApplicationContext) = context.deleteAllUsers()