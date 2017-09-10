package dev.yn.playground.user

import dev.yn.playground.sql.SQLTransactionExecutor
import io.vertx.core.Future
import dev.yn.playground.sql.extensions.execution.*

class UserService(val sqlTransactionExecutor: SQLTransactionExecutor) {
    fun createUser(user: UserAndPassword): Future<UserAndPassword> = update(sqlTransactionExecutor, UserTransactions.createUserTransaction, user)
    fun loginUser(userNameAndPassword: UserNameAndPassword): Future<String> = query(sqlTransactionExecutor, UserTransactions.authenticateUserTransaction, userNameAndPassword)
}