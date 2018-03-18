package playwright.skript.user.props

import org.funktionale.option.Option
import playwright.skript.auth.AuthSession
import playwright.skript.auth.props.UserSessionProps
import playwright.skript.user.models.UserProfile

interface ExistingUserProfileProps {
    fun getExistingProfile(): Option<UserProfile>
    fun useProfile(profile: UserProfile)
}

class GetUserProps(val sessionKey: String, var session: Option<AuthSession> = Option.None): UserSessionProps {
    override fun getUserSessionKey(): String = sessionKey

    override fun setUserSession(userSession: AuthSession) {
        session = Option.Some(userSession)
    }

    override fun getUserSession(): Option<AuthSession> = session
}