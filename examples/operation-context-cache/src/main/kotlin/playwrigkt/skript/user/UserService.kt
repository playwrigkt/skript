package playwrigkt.skript.user

import playwrigkt.skript.common.ApplicationStageManager
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.user.models.UserNameAndPassword
import playwrigkt.skript.user.models.UserProfile
import playwrigkt.skript.user.models.UserProfileAndPassword
import playwrigkt.skript.user.models.UserSession
import playwrigkt.skript.user.props.GetUserTroupeProps

class UserService(val provider: ApplicationStageManager) {
    fun createUser(userProfile: UserProfileAndPassword): AsyncResult<UserProfile> =
            provider.runWithTroupe(UserSkripts.CREATE_USER_SKRIPT, userProfile, Unit)

    fun loginUser(userNameAndPassword: UserNameAndPassword): AsyncResult<UserSession> =
            provider.runWithTroupe(UserSkripts.LOGIN_USER_SKRIPT, userNameAndPassword, Unit)

    fun getUser(userId: String, token: String): AsyncResult<UserProfile> =
            provider.runWithTroupe(UserSkripts.GET_USER_SKRIPT, userId, GetUserTroupeProps(token))
}
