package dev.yn.playground.user.extensions.schema

import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.user.sql.UserSchema
import dev.yn.playground.sql.transaction.SQLTransactionSkript

fun ApplicationContext.initUserSchema() = SQLTransactionSkript.transaction(UserSchema.init()).run(Unit, this)
fun ApplicationContext.dropUserSchema() = SQLTransactionSkript.autoCommit(UserSchema.drop()).run(Unit, this)

fun <T> AsyncResult<T>.dropUserSchema(context: ApplicationContext) = this.flatMap { context.dropUserSchema() }
fun <T> AsyncResult<T>.initUserSchema(context: ApplicationContext) = this.flatMap{ context.initUserSchema() }