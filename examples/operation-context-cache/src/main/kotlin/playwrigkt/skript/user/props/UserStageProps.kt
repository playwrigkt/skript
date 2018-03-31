package playwrigkt.skript.user.props

import org.funktionale.option.Option
import playwrigkt.skript.auth.AuthSession
import playwrigkt.skript.auth.props.UserSessionStageProps
import playwrigkt.skript.user.models.UserProfile

interface ExistingUserProfileStageProps {
    fun getExistingProfile(): Option<UserProfile>
    fun useProfile(profile: UserProfile)
}

class GetUserStageProps(val sessionKey: String, var session: Option<AuthSession> = Option.None): UserSessionStageProps {
    override fun getUserSessionKey(): String = sessionKey

    override fun setUserSession(userSession: AuthSession) {
        session = Option.Some(userSession)
    }

    override fun getUserSession(): Option<AuthSession> = session
}