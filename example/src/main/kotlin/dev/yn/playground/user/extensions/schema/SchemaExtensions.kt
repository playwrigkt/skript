package dev.yn.playground.user.extensions.schema

import dev.yn.playground.sql.task.SQLClientProvider
import dev.yn.playground.sql.task.UnpreparedSQLTask
import dev.yn.playground.user.sql.UserSchema
import io.vertx.core.Future

fun <P: SQLClientProvider> P.initUserSchema() = UnpreparedSQLTask(UserSchema.init()).prepare(this).run(Unit)
fun <P: SQLClientProvider> P.dropUserSchema() = UnpreparedSQLTask(UserSchema.drop()).prepare(this).run(Unit)

fun <T, P: SQLClientProvider> Future<T>.dropUserSchema(provider: P) = this.compose { provider.dropUserSchema() }
fun <T, P: SQLClientProvider> Future<T>.initUserSchema(provider: P) = this.compose { provider.initUserSchema() }