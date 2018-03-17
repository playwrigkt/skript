package dev.yn.playground.user.extensions.transaction

import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.user.UserTasks
import dev.yn.playground.sql.transaction.SQLTransactionSkript

fun ApplicationContext<Unit>.deleteAllUsers() = SQLTransactionSkript.autoCommit<Unit, Unit, ApplicationContext<Unit>>(UserTasks.deleteAllUserActionChain).run(Unit, this)