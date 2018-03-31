package playwrigkt.skript.user.extensions.schema

import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.sql.transaction.SQLTransactionSkript
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.sql.UserSchema

fun ApplicationTroupe.initUserSchema() = SQLTransactionSkript.transaction(UserSchema.init()).run(Unit, this)
fun ApplicationTroupe.dropUserSchema() = SQLTransactionSkript.autoCommit(UserSchema.drop()).run(Unit, this)

fun <T> AsyncResult<T>.dropUserSchema(stage: ApplicationTroupe) = this.flatMap { stage.dropUserSchema() }
fun <T> AsyncResult<T>.initUserSchema(stage: ApplicationTroupe) = this.flatMap{ stage.initUserSchema() }