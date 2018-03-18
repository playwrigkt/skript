package playwright.skript.chatrooom

import playwright.skript.chatrooom.models.ChatRoom
import playwright.skript.chatrooom.models.ChatRoomPermissions
import playwright.skript.chatrooom.models.ChatRoomUser
import playwright.skript.chatrooom.props.ChatroomOperationProps
import playwright.skript.common.ApplicationVenue
import playwright.skript.result.AsyncResult

class ChatRoomService(val provider: ApplicationVenue) {

    fun addUser(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnStage(ChatRoomSkripts.ADD_USER_SKRIPT, chatRoomUser, ChatroomOperationProps(sessionKey))

    fun removeUser(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnStage(ChatRoomSkripts.REMOVE_USER_SKRIPT, chatRoomUser, ChatroomOperationProps(sessionKey))

    fun addUserPermissions(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnStage(ChatRoomSkripts.ADD_USER_PERMISSIONS_SKRIPT, chatRoomUser, ChatroomOperationProps(sessionKey))

    fun removeUserPermissions(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnStage(ChatRoomSkripts.REMOVE_USER_PERMISSIONS_SKRIPT, chatRoomUser, ChatroomOperationProps(sessionKey))

    fun addPublicPermissions(chatRoomPermissions: ChatRoomPermissions, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnStage(ChatRoomSkripts.ADD_PUBLIC_PERMISSION_SKRIPT, chatRoomPermissions, ChatroomOperationProps(sessionKey))

    fun removePublicPermissions(chatRoomPermissions: ChatRoomPermissions, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnStage(ChatRoomSkripts.REMOVE_PUBLIC_PERMISSION_SKRIPT, chatRoomPermissions, ChatroomOperationProps(sessionKey))

    fun createChatRoom(chatRoom: ChatRoom, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnStage(ChatRoomSkripts.CREATE_CHAT_ROOM_SKRIPT, chatRoom, ChatroomOperationProps(sessionKey))

    fun updateChatRoom(chatRoom: ChatRoom, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnStage(ChatRoomSkripts.UPDATE_CHAT_ROOM_SKRIPT, chatRoom, ChatroomOperationProps(sessionKey))

    fun getChatRoom(chatRoomId: String, sessionKey: String): AsyncResult<ChatRoom> =
            provider.runOnStage(ChatRoomSkripts.GET_CHATROOM_SKRIPT, chatRoomId, ChatroomOperationProps(sessionKey))

}