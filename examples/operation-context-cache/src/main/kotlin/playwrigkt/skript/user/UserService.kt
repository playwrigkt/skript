package playwrigkt.skript.user

import playwrigkt.skript.common.ApplicationVenue
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.user.models.UserNameAndPassword
import playwrigkt.skript.user.models.UserProfile
import playwrigkt.skript.user.models.UserProfileAndPassword
import playwrigkt.skript.user.models.UserSession
import playwrigkt.skript.user.props.GetUserStageProps

class UserService(val provider: ApplicationVenue) {
    fun createUser(userProfile: UserProfileAndPassword): AsyncResult<UserProfile> =
            provider.runOnStage(UserSkripts.CREATE_USER_SKRIPT, userProfile, Unit)

    fun loginUser(userNameAndPassword: UserNameAndPassword): AsyncResult<UserSession> =
            provider.runOnStage(UserSkripts.LOGIN_USER_SKRIPT, userNameAndPassword, Unit)

    fun getUser(userId: String, token: String): AsyncResult<UserProfile> =
            provider.runOnStage(UserSkripts.GET_USER_SKRIPT, userId, GetUserStageProps(token))
}
