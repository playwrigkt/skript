package playwrigkt.skript.user.extensions.transaction

import playwrigkt.skript.common.ApplicationStage
import playwrigkt.skript.sql.transaction.SQLTransactionSkript
import playwrigkt.skript.user.UserSkripts

fun ApplicationStage.deleteAllUsers() = SQLTransactionSkript.autoCommit<Unit, Unit, ApplicationStage>(UserSkripts.deleteAllUserActionChain()).run(Unit, this)