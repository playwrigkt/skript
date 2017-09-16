package dev.yn.playground.user.extensions.schema

import dev.yn.playground.sql.task.SQLTask
import dev.yn.playground.user.UserSchema
import io.vertx.core.Future
import io.vertx.ext.sql.SQLClient

fun SQLClient.initUserSchema() = SQLTask(UserSchema.init, this).run(Unit)
fun SQLClient.dropUserSchema() = SQLTask(UserSchema.drop, this).run(Unit)

fun <T> Future<T>.dropUserSchema(client: SQLClient) = this.compose { client.dropUserSchema() }
fun <T> Future<T>.initUserSchema(client: SQLClient) = this.compose { client.initUserSchema() }