package playwrigkt.skript.chatrooom.props

import playwrigkt.skript.Skript
import playwrigkt.skript.chatrooom.models.ChatroomId
import playwrigkt.skript.chatrooom.sql.query.GetChatRoom
import playwrigkt.skript.common.ApplicationStage
import playwrigkt.skript.ex.query

object ChatroomPropsSkripts {
    fun <I: ChatroomId> hydrateExistingChatroom(): Skript<I, I, ApplicationStage<ChatroomStageProps>> =
            Skript.updateStage(
                    Skript.identity<I, ApplicationStage<ChatroomStageProps>>()
                            .map { it.getChatroomId() }
                            .query(GetChatRoom)
                            .mapWithStage { chatroom, stage -> stage.getStageProps().useChatroom(chatroom) })

    fun hydrateExistingChatroomById(): Skript<String, String, ApplicationStage<ChatroomStageProps>> =
            Skript.updateStage(
                    Skript.identity<String, ApplicationStage<ChatroomStageProps>>()
                            .query(GetChatRoom)
                            .mapWithStage { chatroom, stage -> stage.getStageProps().useChatroom(chatroom) })
}