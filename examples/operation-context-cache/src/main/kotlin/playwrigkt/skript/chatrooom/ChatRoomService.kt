package playwrigkt.skript.chatrooom

import playwrigkt.skript.chatrooom.models.ChatRoom
import playwrigkt.skript.chatrooom.models.ChatRoomPermissions
import playwrigkt.skript.chatrooom.models.ChatRoomUser
import playwrigkt.skript.chatrooom.props.ChatroomStageProps
import playwrigkt.skript.common.ApplicationVenue
import playwrigkt.skript.result.AsyncResult

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