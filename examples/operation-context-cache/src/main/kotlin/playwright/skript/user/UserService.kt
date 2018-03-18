package playwright.skript.user

import playwright.skript.common.ApplicationVenue
import playwright.skript.result.AsyncResult
import playwright.skript.user.models.UserNameAndPassword
import playwright.skript.user.models.UserProfile
import playwright.skript.user.models.UserProfileAndPassword
import playwright.skript.user.models.UserSession
import playwright.skript.user.props.GetUserProps

class UserService(val provider: ApplicationVenue) {
    fun createUser(userProfile: UserProfileAndPassword): AsyncResult<UserProfile> =
            provider.runOnContext(UserSkripts.CREATE_USER_SKRIPT, userProfile, Unit)

    fun loginUser(userNameAndPassword: UserNameAndPassword): AsyncResult<UserSession> =
            provider.runOnContext(UserSkripts.LOGIN_USER_SKRIPT, userNameAndPassword, Unit)

    fun getUser(userId: String, token: String): AsyncResult<UserProfile> =
            provider.runOnContext(UserSkripts.GET_USER_SKRIPT, userId, GetUserProps(token))
}
