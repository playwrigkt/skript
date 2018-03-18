package playwright.skript.chatrooom

import playwright.skript.chatrooom.models.ChatRoom
import playwright.skript.chatrooom.models.ChatRoomPermissions
import playwright.skript.chatrooom.models.ChatRoomUser
import playwright.skript.chatrooom.props.ChatroomStageProps
import playwright.skript.common.ApplicationVenue
import playwright.skript.result.AsyncResult

class ChatRoomService(val venue: ApplicationVenue) {

    fun addUser(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runOnStage(ChatRoomSkripts.ADD_USER_SKRIPT, chatRoomUser, ChatroomStageProps(sessionKey))

    fun removeUser(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runOnStage(ChatRoomSkripts.REMOVE_USER_SKRIPT, chatRoomUser, ChatroomStageProps(sessionKey))

    fun addUserPermissions(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runOnStage(ChatRoomSkripts.ADD_USER_PERMISSIONS_SKRIPT, chatRoomUser, ChatroomStageProps(sessionKey))

    fun removeUserPermissions(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runOnStage(ChatRoomSkripts.REMOVE_USER_PERMISSIONS_SKRIPT, chatRoomUser, ChatroomStageProps(sessionKey))

    fun addPublicPermissions(chatRoomPermissions: ChatRoomPermissions, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runOnStage(ChatRoomSkripts.ADD_PUBLIC_PERMISSION_SKRIPT, chatRoomPermissions, ChatroomStageProps(sessionKey))

    fun removePublicPermissions(chatRoomPermissions: ChatRoomPermissions, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runOnStage(ChatRoomSkripts.REMOVE_PUBLIC_PERMISSION_SKRIPT, chatRoomPermissions, ChatroomStageProps(sessionKey))

    fun createChatRoom(chatRoom: ChatRoom, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runOnStage(ChatRoomSkripts.CREATE_CHAT_ROOM_SKRIPT, chatRoom, ChatroomStageProps(sessionKey))

    fun updateChatRoom(chatRoom: ChatRoom, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runOnStage(ChatRoomSkripts.UPDATE_CHAT_ROOM_SKRIPT, chatRoom, ChatroomStageProps(sessionKey))

    fun getChatRoom(chatRoomId: String, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runOnStage(ChatRoomSkripts.GET_CHATROOM_SKRIPT, chatRoomId, ChatroomStageProps(sessionKey))

}