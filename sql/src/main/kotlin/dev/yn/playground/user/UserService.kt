package dev.yn.playground.user

import dev.yn.playground.sql.SQLTransactionExecutor
import dev.yn.playground.task.UnpreparedSQLUpdateTask
import io.vertx.core.Future

class UserService(val sqlTransactionExecutor: SQLTransactionExecutor) {

    val updateTask = UnpreparedSQLUpdateTask(UserTransactions.login)
    val createTask = UnpreparedSQLUpdateTask(UserTransactions.createUserTransaction)

    fun createUser(userProfile: UserProfileAndPassword): Future<UserProfileAndPassword> = createTask.prepare(sqlTransactionExecutor).run(userProfile)
    fun loginUser(userNameAndPassword: UserNameAndPassword): Future<UserSession> = updateTask.prepare(sqlTransactionExecutor).run(userNameAndPassword)
}