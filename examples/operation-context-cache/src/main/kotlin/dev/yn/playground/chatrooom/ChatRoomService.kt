package dev.yn.playground.chatrooom

import dev.yn.playground.chatrooom.context.ChatroomOperationCache
import dev.yn.playground.chatrooom.models.ChatRoom
import dev.yn.playground.chatrooom.models.ChatRoomPermissions
import dev.yn.playground.chatrooom.models.ChatRoomUser
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.result.AsyncResult

class ChatRoomService(val provider: ApplicationContextProvider) {

    fun addUser(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.ADD_USER_SKRIPT, chatRoomUser, ChatroomOperationCache(sessionKey))

    fun removeUser(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.REMOVE_USER_SKRIPT, chatRoomUser, ChatroomOperationCache(sessionKey))

    fun addUserPermissions(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.ADD_USER_PERMISSIONS_SKRIPT, chatRoomUser, ChatroomOperationCache(sessionKey))

    fun removeUserPermissions(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.REMOVE_USER_PERMISSIONS_SKRIPT, chatRoomUser, ChatroomOperationCache(sessionKey))

    fun addPublicPermissions(chatRoomPermissions: ChatRoomPermissions, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.ADD_PUBLIC_PERMISSION_SKRIPT, chatRoomPermissions, ChatroomOperationCache(sessionKey))

    fun removePublicPermissions(chatRoomPermissions: ChatRoomPermissions, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.REMOVE_PUBLIC_PERMISSION_SKRIPT, chatRoomPermissions, ChatroomOperationCache(sessionKey))

    fun createChatRoom(chatRoom: ChatRoom, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.CREATE_CHAT_ROOM_SKRIPT, chatRoom, ChatroomOperationCache(sessionKey))

    fun updateChatRoom(chatRoom: ChatRoom, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.UPDATE_CHAT_ROOM_SKRIPT, chatRoom, ChatroomOperationCache(sessionKey))

    fun getChatRoom(chatRoomId: String, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnContext(ChatRoomTasks.GET_CHATROOM_SKRIPT, chatRoomId, ChatroomOperationCache(sessionKey))

}