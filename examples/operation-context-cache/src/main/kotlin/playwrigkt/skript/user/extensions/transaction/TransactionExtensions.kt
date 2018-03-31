package playwrigkt.skript.user.extensions.transaction

import playwrigkt.skript.common.ApplicationStage
import playwrigkt.skript.sql.transaction.SQLTransactionSkript
import playwrigkt.skript.user.UserSkripts

fun ApplicationStage<Unit>.deleteAllUsers() = SQLTransactionSkript.autoCommit<Unit, Unit, ApplicationStage<Unit>>(UserSkripts.deleteAllUserActionChain).run(Unit, this)