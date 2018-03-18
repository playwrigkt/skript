package playwright.skript.user.extensions.schema

import playwright.skript.common.ApplicationStage
import playwright.skript.result.AsyncResult
import playwright.skript.sql.transaction.SQLTransactionSkript
import playwright.skript.user.sql.UserSchema

fun ApplicationStage.initUserSchema() = SQLTransactionSkript.transaction(UserSchema.init()).run(Unit, this)
fun ApplicationStage.dropUserSchema() = SQLTransactionSkript.autoCommit(UserSchema.drop()).run(Unit, this)

fun <T> AsyncResult<T>.dropUserSchema(context: ApplicationStage) = this.flatMap { context.dropUserSchema() }
fun <T> AsyncResult<T>.initUserSchema(context: ApplicationStage) = this.flatMap{ context.initUserSchema() }