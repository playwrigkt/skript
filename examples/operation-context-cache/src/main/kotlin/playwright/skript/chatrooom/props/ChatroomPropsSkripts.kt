package playwright.skript.chatrooom.props

import playwright.skript.Skript
import playwright.skript.chatrooom.models.ChatroomId
import playwright.skript.chatrooom.sql.query.GetChatRoom
import playwright.skript.common.ApplicationStage
import playwright.skript.ex.query

object ChatroomPropsSkripts {
    fun <I: ChatroomId> hydrateExistingChatroom(): Skript<I, I, ApplicationStage<ChatroomOperationProps>> =
            Skript.updateContext(
                    Skript.identity<I, ApplicationStage<ChatroomOperationProps>>()
                            .map { it.getChatroomId() }
                            .query(GetChatRoom)
                            .mapWithContext { chatroom, context -> context.getStageProps().useChatroom(chatroom) })

    fun hydrateExistingChatroomById(): Skript<String, String, ApplicationStage<ChatroomOperationProps>> =
            Skript.updateContext(
                    Skript.identity<String, ApplicationStage<ChatroomOperationProps>>()
                            .query(GetChatRoom)
                            .mapWithContext { chatroom, context -> context.getStageProps().useChatroom(chatroom) })
}