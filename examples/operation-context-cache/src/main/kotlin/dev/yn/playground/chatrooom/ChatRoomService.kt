package dev.yn.playground.chatrooom

import dev.yn.playground.Task
import dev.yn.playground.auth.context.UserSessionCache
import dev.yn.playground.chatrooom.context.ChatroomOperationCache
import dev.yn.playground.chatrooom.models.ChatRoom
import dev.yn.playground.chatrooom.models.ChatRoomPermissions
import dev.yn.playground.chatrooom.models.ChatRoomUser
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.result.AsyncResult
import devyn.playground.sql.task.SQLTransactionTask

class ChatRoomService(val provider: ApplicationContextProvider) {

    fun addUser(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.addUserTask, chatRoomUser, ChatroomOperationCache(sessionKey))

    fun removeUser(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.removeUserTask, chatRoomUser, ChatroomOperationCache(sessionKey))

    fun addUserPermissions(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.addUserPermissionsTask, chatRoomUser, ChatroomOperationCache(sessionKey))

    fun removeUserPermissions(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.removeUserPermissionsTask, chatRoomUser, ChatroomOperationCache(sessionKey))

    fun addPublicPermissions(chatRoomPermissions: ChatRoomPermissions, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.addPublicPermissionTask, chatRoomPermissions, ChatroomOperationCache(sessionKey))

    fun removePublicPermissions(chatRoomPermissions: ChatRoomPermissions, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.removePublicPermissionTask, chatRoomPermissions, ChatroomOperationCache(sessionKey))

    fun createChatRoom(chatRoom: ChatRoom, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.createChatRoomTask, chatRoom, ChatroomOperationCache(sessionKey))

    fun updateChatRoom(chatRoom: ChatRoom, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.updateChatRoomTask, chatRoom, ChatroomOperationCache(sessionKey))

    fun getChatRoom(chatRoomId: String, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.getChatroomTask, chatRoomId, ChatroomOperationCache(sessionKey))

}