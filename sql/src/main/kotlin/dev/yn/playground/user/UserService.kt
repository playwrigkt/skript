package dev.yn.playground.user

import dev.yn.playground.sql.SQLTransactionExecutor
import io.vertx.core.Future

class UserService(val sqlTransactionExecutor: SQLTransactionExecutor) {
    fun createUser(user: UserAndPassword): Future<UserAndPassword> = sqlTransactionExecutor.update(user, UserTransactions.createUserTransaction)
    fun loginUser(userNameAndPassword: UserNameAndPassword): Future<String> = sqlTransactionExecutor.query(userNameAndPassword, UserTransactions.authenticateUserTransaction)
}