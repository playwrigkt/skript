package dev.yn.playground.user

import dev.yn.playground.sql.SQLTransactionExecutor
import io.vertx.core.Future
import dev.yn.playground.sql.extensions.execution.*

class UserService(val sqlTransactionExecutor: SQLTransactionExecutor) {
    fun createUser(userProfile: UserProfileAndPassword): Future<UserProfileAndPassword> = update(sqlTransactionExecutor, UserTransactions.createUserTransaction, userProfile)
    fun loginUser(userNameAndPassword: UserNameAndPassword): Future<UserSession> = sqlTransactionExecutor.update(userNameAndPassword, UserTransactions.login)
}