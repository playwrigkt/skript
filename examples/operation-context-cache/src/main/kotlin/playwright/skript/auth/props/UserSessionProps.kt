package playwright.skript.auth.props

import org.funktionale.option.Option
import playwright.skript.auth.AuthSession

interface UserSessionProps {
    companion object {
        fun create(sessionKey: String): UserSessionProps = NothingElse(sessionKey)
    }

    fun getUserSessionKey(): String
    fun setUserSession(userSession: AuthSession)
    fun getUserSession(): Option<AuthSession>

    private data class NothingElse(val sessionKey: String, var session: Option<AuthSession> = Option.None): UserSessionProps {
        override fun getUserSessionKey(): String = sessionKey

        override fun setUserSession(userSession: AuthSession) {
            this.session = Option.Some(userSession)
        }

        override fun getUserSession(): Option<AuthSession> = session
    }
}