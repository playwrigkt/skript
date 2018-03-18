package playwright.skript.user

import playwright.skript.common.ApplicationVenue
import playwright.skript.result.AsyncResult
import playwright.skript.user.models.UserNameAndPassword
import playwright.skript.user.models.UserProfile
import playwright.skript.user.models.UserProfileAndPassword
import playwright.skript.user.models.UserSession
import playwright.skript.user.props.GetUserStageProps

class UserService(val provider: ApplicationVenue) {
    fun createUser(userProfile: UserProfileAndPassword): AsyncResult<UserProfile> =
            provider.runOnStage(UserSkripts.CREATE_USER_SKRIPT, userProfile, Unit)

    fun loginUser(userNameAndPassword: UserNameAndPassword): AsyncResult<UserSession> =
            provider.runOnStage(UserSkripts.LOGIN_USER_SKRIPT, userNameAndPassword, Unit)

    fun getUser(userId: String, token: String): AsyncResult<UserProfile> =
            provider.runOnStage(UserSkripts.GET_USER_SKRIPT, userId, GetUserStageProps(token))
}
