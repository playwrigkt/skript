package playwright.skript.user.extensions.schema

import playwright.skript.common.ApplicationStage
import playwright.skript.result.AsyncResult
import playwright.skript.sql.transaction.SQLTransactionSkript
import playwright.skript.user.sql.UserSchema

fun ApplicationStage<Unit>.initUserSchema() = SQLTransactionSkript.transaction<Unit, Unit, ApplicationStage<Unit>>(UserSchema.init()).run(Unit, this)
fun ApplicationStage<Unit>.dropUserSchema() = SQLTransactionSkript.autoCommit<Unit, Unit, ApplicationStage<Unit>>(UserSchema.drop()).run(Unit, this)

fun <T> AsyncResult<T>.dropUserSchema(stage: ApplicationStage<Unit>) = this.flatMap { stage.dropUserSchema() }
fun <T> AsyncResult<T>.initUserSchema(stage: ApplicationStage<Unit>) = this.flatMap{ stage.initUserSchema() }