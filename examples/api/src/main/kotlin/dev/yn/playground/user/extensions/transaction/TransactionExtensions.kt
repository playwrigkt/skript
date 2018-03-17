package dev.yn.playground.user.extensions.transaction

import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.user.UserSkripts
import dev.yn.playground.sql.transaction.SQLTransactionSkript

fun ApplicationContext.deleteAllUsers() = SQLTransactionSkript.autoCommit<Unit, Unit, ApplicationContext>(UserSkripts.deleteAllUserActionChain()).run(Unit, this)