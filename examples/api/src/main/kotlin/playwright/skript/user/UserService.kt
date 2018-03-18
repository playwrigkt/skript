package playwright.skript.user

import playwright.skript.common.ApplicationVenue
import playwright.skript.result.AsyncResult
import playwright.skript.user.models.UserNameAndPassword
import playwright.skript.user.models.UserProfile
import playwright.skript.user.models.UserProfileAndPassword
import playwright.skript.user.models.UserSession

class UserService(val provider: ApplicationVenue) {
    fun createUser(userProfile: UserProfileAndPassword): AsyncResult<UserProfile> =
            provider.runOnStage(UserSkripts.UNPREPARED_CREATE_SKRIPT, userProfile)

    fun loginUser(userNameAndPassword: UserNameAndPassword): AsyncResult<UserSession> =
            provider.runOnStage(UserSkripts.UNPREPARED_LOGIN_SKRIPT, userNameAndPassword)

    fun getUser(userId: String, token: String): AsyncResult<UserProfile> =
            provider.runOnStage(UserSkripts.UNPREPARED_GET_SKRIPT, playwright.skript.auth.TokenAndInput(token, userId))
}
