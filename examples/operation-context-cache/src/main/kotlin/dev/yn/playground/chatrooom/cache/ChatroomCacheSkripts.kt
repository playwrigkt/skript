package dev.yn.playground.chatrooom.cache

import dev.yn.playground.Skript
import dev.yn.playground.chatrooom.context.ChatroomOperationCache
import dev.yn.playground.chatrooom.models.ChatroomId
import dev.yn.playground.chatrooom.sql.query.GetChatRoom
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.ex.query

object ChatroomCacheSkripts {
    fun <I: ChatroomId> hydrateExistingChatroom(): Skript<I, I, ApplicationContext<ChatroomOperationCache>> =
            Skript.updateContext(
                    Skript.identity<I, ApplicationContext<ChatroomOperationCache>>()
                            .map { it.getChatroomId() }
                            .query(GetChatRoom)
                            .mapWithContext { chatroom, context -> context.cache.useChatroom(chatroom) })

    fun hydrateExistingChatroomById(): Skript<String, String, ApplicationContext<ChatroomOperationCache>> =
            Skript.updateContext(
                    Skript.identity<String, ApplicationContext<ChatroomOperationCache>>()
                            .query(GetChatRoom)
                            .mapWithContext { chatroom, context -> context.cache.useChatroom(chatroom) })
}