package dev.yn.playground.user.extensions.transaction

import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.user.UserTasks
import devyn.playground.sql.task.SQLTransactionTask

fun ApplicationContext.deleteAllUsers() = SQLTransactionTask.autoCommit<Unit, Unit, ApplicationContext>(UserTasks.deleteAllUserActionChain()).run(Unit, this)