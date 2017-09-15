package dev.yn.playground.user

import dev.yn.playground.sql.SQLTransactionExecutor
import dev.yn.playground.sql.extensions.task.*
import dev.yn.playground.task.*
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx

class SQLAndVertxProvider(val vertx: Vertx, val sqlTransactionExecutor: SQLTransactionExecutor) : SQLTransactionExecutorProvider, VertxProvider {
    override fun provideVertx(): Vertx = vertx

    override fun provideSQLTransactionExecutor(): SQLTransactionExecutor {
        return sqlTransactionExecutor
    }
}


class UserService(val sqlTransactionExecutor: SQLTransactionExecutor, val vertx: Vertx) {

    val unpreparedCreateTask: UnpreparedTask<UserProfileAndPassword, UserProfile, SQLAndVertxProvider> =
            SQLTask.unpreparedTransactionalSql<UserProfileAndPassword, UserProfile, SQLAndVertxProvider>(UserTransactions.createUserTransaction)
                    .vertxAsync(VertxTask.sendWithResponse(userCreatedAddress))

    val unpreparedLoginTask: UnpreparedTask<UserNameAndPassword, UserSession, SQLAndVertxProvider> =
            SQLTask.unpreparedTransactionalSql<UserNameAndPassword, UserSession, SQLAndVertxProvider>(UserTransactions.loginTransaction)

    val unpreparedGetTask: UnpreparedTask<TokenAndInput<String>, UserProfile, SQLAndVertxProvider> =
            SQLTask.unpreparedSql(UserTransactions.getUserTransaction)

    val provider = SQLAndVertxProvider(vertx, sqlTransactionExecutor)

    val createTask: Task<UserProfileAndPassword, UserProfile> = unpreparedCreateTask.prepare(provider)
    val loginTask: Task<UserNameAndPassword, UserSession> = unpreparedLoginTask.prepare(provider)
    val getTask: Task<TokenAndInput<String>, UserProfile> = unpreparedGetTask.prepare(provider)

    init {
        vertx.deployVerticle(UserUpdatedProcessingVerticle())
    }

    fun createUser(userProfile: UserProfileAndPassword): Future<UserProfile> = createTask.run(userProfile)
    fun loginUser(userNameAndPassword: UserNameAndPassword): Future<UserSession> = loginTask.run(userNameAndPassword)
    fun getUser(userId: String, token: String): Future<UserProfile> = getTask.run(TokenAndInput(token, userId))
}
val userCreatedAddress = "user.updated"

class UserUpdatedProcessingVerticle: AbstractVerticle() {
    override fun start() {
        vertx.eventBus().consumer<String>(userCreatedAddress) {
            println("it was updated: ${it.body()}")
            it.reply("thanks")
        }
    }
}