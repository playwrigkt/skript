package playwrigkt.skript.user.extensions.schema

import playwrigkt.skript.common.ApplicationStage
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.sql.transaction.SQLTransactionSkript
import playwrigkt.skript.user.sql.UserSchema

fun ApplicationStage<Unit>.initUserSchema() = SQLTransactionSkript.transaction<Unit, Unit, ApplicationStage<Unit>>(UserSchema.init()).run(Unit, this)
fun ApplicationStage<Unit>.dropUserSchema() = SQLTransactionSkript.autoCommit<Unit, Unit, ApplicationStage<Unit>>(UserSchema.drop()).run(Unit, this)

fun <T> AsyncResult<T>.dropUserSchema(stage: ApplicationStage<Unit>) = this.flatMap { stage.dropUserSchema() }
fun <T> AsyncResult<T>.initUserSchema(stage: ApplicationStage<Unit>) = this.flatMap{ stage.initUserSchema() }