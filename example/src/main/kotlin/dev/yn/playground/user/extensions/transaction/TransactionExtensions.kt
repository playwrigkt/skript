package dev.yn.playground.user.extensions.transaction

import dev.yn.playground.sql.task.SQLTask
import dev.yn.playground.user.UserTransactions
import io.vertx.core.Future
import io.vertx.ext.sql.SQLClient

fun SQLClient.deleteAllUsers() = SQLTask(UserTransactions.deleteAllUsersTransaction, this).run(Unit)
fun <T> Future<T>.deleteAllUsers(client: SQLClient) = client.deleteAllUsers()