package dev.yn.playground.user

import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.task.result.AsyncResult
import dev.yn.playground.user.context.GetUserContext
import dev.yn.playground.user.models.*

class UserService(val provider: ApplicationContextProvider) {
    fun createUser(userProfile: UserProfileAndPassword): AsyncResult<UserProfile> =
            provider.runOnContext(UserTasks.unpreparedCreateTask, userProfile, Unit)

    fun loginUser(userNameAndPassword: UserNameAndPassword): AsyncResult<UserSession> =
            provider.runOnContext(UserTasks.unpreparedLoginTask, userNameAndPassword, Unit)

    fun getUser(userId: String, token: String): AsyncResult<UserProfile> =
            provider.runOnContext(UserTasks.unpreparedGetTask, userId, GetUserContext(token))
}
