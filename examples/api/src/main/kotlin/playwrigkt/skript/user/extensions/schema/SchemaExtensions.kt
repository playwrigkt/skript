package playwrigkt.skript.user.extensions.schema

import playwrigkt.skript.common.ApplicationStage
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.sql.transaction.SQLTransactionSkript
import playwrigkt.skript.user.sql.UserSchema

fun ApplicationStage.initUserSchema() = SQLTransactionSkript.transaction(UserSchema.init()).run(Unit, this)
fun ApplicationStage.dropUserSchema() = SQLTransactionSkript.autoCommit(UserSchema.drop()).run(Unit, this)

fun <T> AsyncResult<T>.dropUserSchema(stage: ApplicationStage) = this.flatMap { stage.dropUserSchema() }
fun <T> AsyncResult<T>.initUserSchema(stage: ApplicationStage) = this.flatMap{ stage.initUserSchema() }