package playwrigkt.skript.user.extensions.transaction

import playwrigkt.skript.sql.transaction.SQLTransactionSkript
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.UserSkripts

fun ApplicationTroupe<Unit>.deleteAllUsers() = SQLTransactionSkript.autoCommit<Unit, Unit, ApplicationTroupe<Unit>>(UserSkripts.deleteAllUserActionChain).run(Unit, this)