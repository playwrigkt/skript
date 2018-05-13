package playwrigkt.skript.user.extensions.schema

import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.sql.transaction.SqlTransactionSkript
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.sql.UserSchema

fun ApplicationTroupe.initUserSchema() = SqlTransactionSkript.transaction(UserSchema.init()).run(Unit, this)
fun ApplicationTroupe.dropUserSchema() = SqlTransactionSkript.autoCommit(UserSchema.drop()).run(Unit, this)

fun <T> AsyncResult<T>.dropUserSchema(stage: ApplicationTroupe) = this.flatMap { stage.dropUserSchema() }
fun <T> AsyncResult<T>.initUserSchema(stage: ApplicationTroupe) = this.flatMap{ stage.initUserSchema() }