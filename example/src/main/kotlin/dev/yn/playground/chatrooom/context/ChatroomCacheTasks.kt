package dev.yn.playground.chatrooom.context

import dev.yn.playground.chatrooom.models.ChatroomId
import dev.yn.playground.chatrooom.sql.query.GetChatRoom
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.sql.ext.query
import dev.yn.playground.task.Task

object ChatroomCacheTasks {
    fun <I: ChatroomId, R: ExistingChatroomCache> hydrateExistingChatroom(): Task<I, I, ApplicationContext<R>> = Task.updateContext(Task.identity<I, ApplicationContext<R>>()
                    .map { it.getChatroomId() }
                    .query(GetChatRoom)
                    .mapWithContext { chatroom, context -> context.cache.useChatroom(chatroom) })

    fun <R: ExistingChatroomCache> hydrateExistingChatroomById(): Task<String, String, ApplicationContext<R>> = Task.updateContext(Task.identity<String, ApplicationContext<R>>()
            .query(GetChatRoom)
            .mapWithContext { chatroom, context -> context.cache.useChatroom(chatroom) })
}