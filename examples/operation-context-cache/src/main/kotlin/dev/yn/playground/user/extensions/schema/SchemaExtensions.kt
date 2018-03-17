package dev.yn.playground.user.extensions.schema

import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.user.sql.UserSchema
import dev.yn.playground.sql.transaction.SQLTransactionSkript

fun ApplicationContext<Unit>.initUserSchema() = SQLTransactionSkript.transaction<Unit, Unit, ApplicationContext<Unit>>(UserSchema.init()).run(Unit, this)
fun ApplicationContext<Unit>.dropUserSchema() = SQLTransactionSkript.autoCommit<Unit, Unit, ApplicationContext<Unit>>(UserSchema.drop()).run(Unit, this)

fun <T> AsyncResult<T>.dropUserSchema(context: ApplicationContext<Unit>) = this.flatMap { context.dropUserSchema() }
fun <T> AsyncResult<T>.initUserSchema(context: ApplicationContext<Unit>) = this.flatMap{ context.initUserSchema() }