package dev.yn.playground.user

import dev.yn.playground.sql.SQLTransactionExecutor
import dev.yn.playground.task.*
import io.vertx.core.Future
import io.vertx.core.Vertx
class SQLAndVertxProvider(val vertx: Vertx, val sqlTransactionExecutor: SQLTransactionExecutor) : SQLTransactionExecutorProvider, VertxProvider {
    override fun provideVertx(): Vertx = vertx

    override fun provideSQLTransactionExecutor(): SQLTransactionExecutor {
        return sqlTransactionExecutor
    }
}


class UserService(val sqlTransactionExecutor: SQLTransactionExecutor, val vertx: Vertx) {
    val provider = SQLAndVertxProvider(vertx, sqlTransactionExecutor)

    val updateTask: Task<UserNameAndPassword, UserSession> = Task.sqlUpdate(UserTransactions.loginTransaction, provider)
    val createTask: Task<UserProfileAndPassword, UserProfile> = Task.sqlUpdate(UserTransactions.createUserTransaction, provider)
    val getTask: Task<TokenAndInput<String>, UserProfile> = Task.sqlUpdate(UserTransactions.getUserTransaction, provider)

    fun createUser(userProfile: UserProfileAndPassword): Future<UserProfile> = createTask.run(userProfile)
    fun loginUser(userNameAndPassword: UserNameAndPassword): Future<UserSession> = updateTask.run(userNameAndPassword)
    fun getUser(userId: String, token: String): Future<UserProfile> = getTask.run(TokenAndInput(token, userId))
}