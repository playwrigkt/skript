package dev.yn.playground.user

import dev.yn.playground.sql.SQLTransactionExecutor
import io.vertx.core.Future

class UserService(val sqlTransactionExecutor: SQLTransactionExecutor) {
    fun createUser(userProfile: UserProfileAndPassword): Future<UserProfileAndPassword> = SQLTransactionExecutor.update(sqlTransactionExecutor, UserTransactions.createUserTransaction, userProfile)
    fun loginUser(userNameAndPassword: UserNameAndPassword): Future<UserSession> = sqlTransactionExecutor.update(userNameAndPassword, UserTransactions.login)
}