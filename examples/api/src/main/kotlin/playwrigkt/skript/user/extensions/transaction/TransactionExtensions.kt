package playwrigkt.skript.user.extensions.transaction

import playwrigkt.skript.sql.transaction.SQLTransactionSkript
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.UserSkripts

fun ApplicationTroupe.deleteAllUsers() = SQLTransactionSkript.autoCommit<Unit, Unit, ApplicationTroupe>(UserSkripts.deleteAllUserActionChain()).run(Unit, this)