package dev.yn.playground.user.extensions.transaction

import dev.yn.playground.sql.task.SQLClientProvider
import dev.yn.playground.sql.task.UnpreparedSQLTask
import dev.yn.playground.user.sql.UserTransactions
import io.vertx.core.Future

fun <P: SQLClientProvider> P.deleteAllUsers() = UnpreparedSQLTask<Unit, Unit, P>(UserTransactions.deleteAllUserActionChain()).prepare(this).run(Unit)
fun <T, P: SQLClientProvider> Future<T>.deleteAllUsers(provider: P) = provider.deleteAllUsers()