package playwrigkt.skript.chatrooom.props

import org.funktionale.option.Option
import playwrigkt.skript.auth.AuthSession
import playwrigkt.skript.auth.props.UserSessionTroupeProps
import playwrigkt.skript.chatrooom.models.ChatRoom

interface ExistingChatroomTroupeProps {
    fun useChatroom(chatroom: ChatRoom)
    fun getChatroom(): Option<ChatRoom>
}

data class ChatroomTroupeProps(
        private val sessionKey: String,
        private var session: Option<AuthSession> = Option.None,
        private var chatroom: Option<ChatRoom> = Option.None)
    : UserSessionTroupeProps, ExistingChatroomTroupeProps {
    override fun useChatroom(chatroom: ChatRoom) {
        this.chatroom = Option.Some(chatroom)
    }

    override fun getChatroom(): Option<ChatRoom> = chatroom

    override fun getUserSessionKey(): String = sessionKey

    override fun setUserSession(userSession: AuthSession) {
        this.session = Option.Some(userSession)
    }

    override fun getUserSession(): Option<AuthSession> = session
}