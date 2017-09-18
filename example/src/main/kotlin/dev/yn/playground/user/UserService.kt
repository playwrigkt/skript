package dev.yn.playground.user

import dev.yn.playground.task.*
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.sql.SQLClient

class UserService(sqlClient: SQLClient, vertx: Vertx) {
    init {
        vertx.deployVerticle(UserUpdatedProcessingVerticle())
        vertx.deployVerticle(UserLoginProcessingVerticle())
    }

    val provider = SQLAndVertxProvider(vertx, sqlClient)

    val createTask: Task<UserProfileAndPassword, UserProfile> = UserTasks.unpreparedCreateTask.prepare(provider)
    val loginTask: Task<UserNameAndPassword, UserSession> = UserTasks.unpreparedLoginTask.prepare(provider)
    val getTask: Task<TokenAndInput<String>, UserProfile> = UserTasks.unpreparedGetTask.prepare(provider)

    fun createUser(userProfile: UserProfileAndPassword): Future<UserProfile> = createTask.run(userProfile)
    fun loginUser(userNameAndPassword: UserNameAndPassword): Future<UserSession> = loginTask.run(userNameAndPassword)
    fun getUser(userId: String, token: String): Future<UserProfile> = getTask.run(TokenAndInput(token, userId))
}
