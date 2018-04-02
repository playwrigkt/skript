package playwrigkt.skript.chatrooom

import playwrigkt.skript.chatrooom.models.ChatRoom
import playwrigkt.skript.chatrooom.models.ChatRoomPermissions
import playwrigkt.skript.chatrooom.models.ChatRoomUser
import playwrigkt.skript.chatrooom.props.ChatroomTroupeProps
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.ApplicationStageManager

class ChatRoomService(val venue: ApplicationStageManager) {

    fun addUser(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runWithTroupe(ChatRoomSkripts.ADD_USER_SKRIPT, chatRoomUser, ChatroomTroupeProps(sessionKey))

    fun removeUser(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runWithTroupe(ChatRoomSkripts.REMOVE_USER_SKRIPT, chatRoomUser, ChatroomTroupeProps(sessionKey))

    fun addUserPermissions(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runWithTroupe(ChatRoomSkripts.ADD_USER_PERMISSIONS_SKRIPT, chatRoomUser, ChatroomTroupeProps(sessionKey))

    fun removeUserPermissions(chatRoomUser: ChatRoomUser, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runWithTroupe(ChatRoomSkripts.REMOVE_USER_PERMISSIONS_SKRIPT, chatRoomUser, ChatroomTroupeProps(sessionKey))

    fun addPublicPermissions(chatRoomPermissions: ChatRoomPermissions, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runWithTroupe(ChatRoomSkripts.ADD_PUBLIC_PERMISSION_SKRIPT, chatRoomPermissions, ChatroomTroupeProps(sessionKey))

    fun removePublicPermissions(chatRoomPermissions: ChatRoomPermissions, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runWithTroupe(ChatRoomSkripts.REMOVE_PUBLIC_PERMISSION_SKRIPT, chatRoomPermissions, ChatroomTroupeProps(sessionKey))

    fun createChatRoom(chatRoom: ChatRoom, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runWithTroupe(ChatRoomSkripts.CREATE_CHAT_ROOM_SKRIPT, chatRoom, ChatroomTroupeProps(sessionKey))

    fun updateChatRoom(chatRoom: ChatRoom, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runWithTroupe(ChatRoomSkripts.UPDATE_CHAT_ROOM_SKRIPT, chatRoom, ChatroomTroupeProps(sessionKey))

    fun getChatRoom(chatRoomId: String, sessionKey: String): AsyncResult<ChatRoom> =
            venue.runWithTroupe(ChatRoomSkripts.GET_CHATROOM_SKRIPT, chatRoomId, ChatroomTroupeProps(sessionKey))

}