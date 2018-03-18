package playwright.skript.user.extensions.transaction

import playwright.skript.common.ApplicationStage
import playwright.skript.sql.transaction.SQLTransactionSkript
import playwright.skript.user.UserSkripts

fun ApplicationStage.deleteAllUsers() = SQLTransactionSkript.autoCommit<Unit, Unit, ApplicationStage>(UserSkripts.deleteAllUserActionChain()).run(Unit, this)