package playwright.skript.user.extensions.schema

import playwright.skript.common.ApplicationStage
import playwright.skript.result.AsyncResult
import playwright.skript.sql.transaction.SQLTransactionSkript
import playwright.skript.user.sql.UserSchema

fun ApplicationStage<Unit>.initUserSchema() = SQLTransactionSkript.transaction<Unit, Unit, ApplicationStage<Unit>>(UserSchema.init()).run(Unit, this)
fun ApplicationStage<Unit>.dropUserSchema() = SQLTransactionSkript.autoCommit<Unit, Unit, ApplicationStage<Unit>>(UserSchema.drop()).run(Unit, this)

fun <T> AsyncResult<T>.dropUserSchema(context: ApplicationStage<Unit>) = this.flatMap { context.dropUserSchema() }
fun <T> AsyncResult<T>.initUserSchema(context: ApplicationStage<Unit>) = this.flatMap{ context.initUserSchema() }