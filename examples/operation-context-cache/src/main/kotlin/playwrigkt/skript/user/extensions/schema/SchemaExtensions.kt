package playwrigkt.skript.user.extensions.schema

import playwrigkt.skript.common.ApplicationTroupe
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.sql.transaction.SQLTransactionSkript
import playwrigkt.skript.user.sql.UserSchema

fun ApplicationTroupe<Unit>.initUserSchema() = SQLTransactionSkript.transaction<Unit, Unit, ApplicationTroupe<Unit>>(UserSchema.init()).run(Unit, this)
fun ApplicationTroupe<Unit>.dropUserSchema() = SQLTransactionSkript.autoCommit<Unit, Unit, ApplicationTroupe<Unit>>(UserSchema.drop()).run(Unit, this)

fun <T> AsyncResult<T>.dropUserSchema(stage: ApplicationTroupe<Unit>) = this.flatMap { stage.dropUserSchema() }
fun <T> AsyncResult<T>.initUserSchema(stage: ApplicationTroupe<Unit>) = this.flatMap{ stage.initUserSchema() }