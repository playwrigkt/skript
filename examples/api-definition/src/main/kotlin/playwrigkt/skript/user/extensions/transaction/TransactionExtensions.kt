package playwrigkt.skript.user.extensions.transaction

import playwrigkt.skript.sql.transaction.SqlTransactionSkript
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.UserSkripts

fun ApplicationTroupe.deleteAllUsers() = SqlTransactionSkript.autoCommit(UserSkripts.deleteAllUserActionChain()).run(Unit, this)