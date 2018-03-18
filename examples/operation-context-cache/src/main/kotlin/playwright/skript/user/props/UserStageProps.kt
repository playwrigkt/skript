package playwright.skript.user.props

import org.funktionale.option.Option
import playwright.skript.auth.AuthSession
import playwright.skript.auth.props.UserSessionStageProps
import playwright.skript.user.models.UserProfile

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