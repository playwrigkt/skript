package playwrigkt.skript.user.extensions.transaction

import playwrigkt.skript.sql.transaction.SqlTransactionSkript
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.UserSkripts

fun ApplicationTroupe.deleteAllUsers() = SqlTransactionSkript.autoCommit<Unit, Unit, ApplicationTroupe>(UserSkripts.deleteAllUserActionChain()).run(Unit, this)