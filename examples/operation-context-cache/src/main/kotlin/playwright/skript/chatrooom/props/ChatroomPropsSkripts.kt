package playwright.skript.chatrooom.props

import playwright.skript.Skript
import playwright.skript.chatrooom.models.ChatroomId
import playwright.skript.chatrooom.sql.query.GetChatRoom
import playwright.skript.common.ApplicationStage
import playwright.skript.ex.query

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