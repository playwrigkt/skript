package dev.yn.playground.user

import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.user.context.GetUserContext
import dev.yn.playground.user.models.UserNameAndPassword
import dev.yn.playground.user.models.UserProfile
import dev.yn.playground.user.models.UserProfileAndPassword
import dev.yn.playground.user.models.UserSession

class UserService(val provider: ApplicationContextProvider) {
    fun createUser(userProfile: UserProfileAndPassword): AsyncResult<UserProfile> =
            provider.runOnContext(UserSkripts.CREATE_USER_SKRIPT, userProfile, Unit)

    fun loginUser(userNameAndPassword: UserNameAndPassword): AsyncResult<UserSession> =
            provider.runOnContext(UserSkripts.LOGIN_USER_SKRIPT, userNameAndPassword, Unit)

    fun getUser(userId: String, token: String): AsyncResult<UserProfile> =
            provider.runOnContext(UserSkripts.GET_USER_SKRIPT, userId, GetUserContext(token))
}
