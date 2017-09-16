package dev.yn.playground.user

import dev.yn.playground.sql.task.SQLClientProvider
import dev.yn.playground.sql.task.SQLTask
import dev.yn.playground.task.*
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.sql.SQLClient

class SQLAndVertxProvider(val vertx: Vertx, val sqlClient: SQLClient) : SQLClientProvider, VertxProvider {
    override fun provideVertx(): Vertx = vertx

    override fun provideSQLClient(): SQLClient{
        return sqlClient
    }
}


class UserService(val sqlClient: SQLClient, val vertx: Vertx) {
    init {
        vertx.deployVerticle(UserUpdatedProcessingVerticle())
    }
    companion object {
        val unpreparedCreateTask: UnpreparedTask<UserProfileAndPassword, UserProfile, SQLAndVertxProvider> =
                SQLTask.unpreparedTransactionalSql<UserProfileAndPassword, UserProfile, SQLAndVertxProvider>(UserTransactions.createUserTransaction)
                        .vertxAsync(VertxTask.sendWithResponse(userCreatedAddress))

        val unpreparedLoginTask: UnpreparedTask<UserNameAndPassword, UserSession, SQLAndVertxProvider> =
                SQLTask.unpreparedTransactionalSql<UserNameAndPassword, UserSession, SQLAndVertxProvider>(UserTransactions.loginTransaction)
                        .vertxAsync(VertxTask.sendAndForget(userLoginAddress))

        val unpreparedGetTask: UnpreparedTask<TokenAndInput<String>, UserProfile, SQLAndVertxProvider> =
                SQLTask.unpreparedSql(UserTransactions.getUserTransaction)
    }

    val provider = SQLAndVertxProvider(vertx, sqlClient)

    val createTask: Task<UserProfileAndPassword, UserProfile> = unpreparedCreateTask.prepare(provider)
    val loginTask: Task<UserNameAndPassword, UserSession> = unpreparedLoginTask.prepare(provider)
    val getTask: Task<TokenAndInput<String>, UserProfile> = unpreparedGetTask.prepare(provider)

    fun createUser(userProfile: UserProfileAndPassword): Future<UserProfile> = createTask.run(userProfile)
    fun loginUser(userNameAndPassword: UserNameAndPassword): Future<UserSession> = loginTask.run(userNameAndPassword)
    fun getUser(userId: String, token: String): Future<UserProfile> = getTask.run(TokenAndInput(token, userId))
}
val userCreatedAddress = "user.updated"
val userLoginAddress = "user.login"

class UserUpdatedProcessingVerticle: AbstractVerticle() {
    override fun start() {
        vertx.eventBus().consumer<String>(userCreatedAddress) {
            println("it was updated: ${it.body()}")
            it.reply("thanks")
        }
    }
}

class UserLoginProcessingVerticle: AbstractVerticle() {
    override fun start() {
        vertx.eventBus().consumer<String>(userLoginAddress) {
            println("it logged in: ${it.body()}")
            it.reply("thanks")
        }
    }
}