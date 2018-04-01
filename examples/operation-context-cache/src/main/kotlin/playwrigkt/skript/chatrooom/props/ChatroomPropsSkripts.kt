package playwrigkt.skript.chatrooom.props

import playwrigkt.skript.Skript
import playwrigkt.skript.chatrooom.models.ChatroomId
import playwrigkt.skript.chatrooom.sql.query.GetChatRoom
import playwrigkt.skript.common.ApplicationTroupe
import playwrigkt.skript.ex.query

object ChatroomPropsSkripts {
    fun <I: ChatroomId> hydrateExistingChatroom(): Skript<I, I, ApplicationTroupe<ChatroomTroupeProps>> =
            Skript.updateTroupe(
                    Skript.identity<I, ApplicationTroupe<ChatroomTroupeProps>>()
                            .map { it.getChatroomId() }
                            .query(GetChatRoom)
                            .mapWithTroupe { chatroom, stage -> stage.getTroupeProps().useChatroom(chatroom) })

    fun hydrateExistingChatroomById(): Skript<String, String, ApplicationTroupe<ChatroomTroupeProps>> =
            Skript.updateTroupe(
                    Skript.identity<String, ApplicationTroupe<ChatroomTroupeProps>>()
                            .query(GetChatRoom)
                            .mapWithTroupe { chatroom, stage -> stage.getTroupeProps().useChatroom(chatroom) })
}