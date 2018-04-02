package playwrigkt.skript.user

import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.user.models.UserNameAndPassword
import playwrigkt.skript.user.models.UserProfile
import playwrigkt.skript.user.models.UserProfileAndPassword
import playwrigkt.skript.user.models.UserSession

class UserService(val provider: ApplicationStageManager) {
    fun createUser(userProfile: UserProfileAndPassword): AsyncResult<UserProfile> =
            provider.runWithTroupe(UserSkripts.UNPREPARED_CREATE_SKRIPT, userProfile)

    fun loginUser(userNameAndPassword: UserNameAndPassword): AsyncResult<UserSession> =
            provider.runWithTroupe(UserSkripts.UNPREPARED_LOGIN_SKRIPT, userNameAndPassword)

    fun getUser(userId: String, token: String): AsyncResult<UserProfile> =
            provider.runWithTroupe(UserSkripts.UNPREPARED_GET_SKRIPT, playwrigkt.skript.auth.TokenAndInput(token, userId))
}
