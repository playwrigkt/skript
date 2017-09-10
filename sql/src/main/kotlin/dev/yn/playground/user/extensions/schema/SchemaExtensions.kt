package dev.yn.playground.user.extensions.schema

import dev.yn.playground.sql.SQLTransactionExecutor
import dev.yn.playground.user.UserSchema
import io.vertx.core.Future

fun SQLTransactionExecutor.initUserSchema() = this.execute(UserSchema.init)
fun SQLTransactionExecutor.dropUserSchema() = this.execute(UserSchema.drop)

fun <T> Future<T>.dropUserSchema(executor: SQLTransactionExecutor) = this.compose { executor.dropUserSchema() }
fun <T> Future<T>.initUserSchema(executor: SQLTransactionExecutor) = this.compose { executor.initUserSchema() }