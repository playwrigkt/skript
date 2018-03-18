package playwright.skript.user.extensions.transaction

import playwright.skript.common.ApplicationStage
import playwright.skript.sql.transaction.SQLTransactionSkript
import playwright.skript.user.UserSkripts

fun ApplicationStage<Unit>.deleteAllUsers() = SQLTransactionSkript.autoCommit<Unit, Unit, ApplicationStage<Unit>>(UserSkripts.deleteAllUserActionChain).run(Unit, this)