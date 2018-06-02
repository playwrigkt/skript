package playwrigkt.skript.chatroom

import playwrigkt.skript.chatroom.models.ChatRoom
import playwrigkt.skript.chatroom.models.ChatRoomPermissionKey
import playwrigkt.skript.chatroom.models.ChatRoomUser
import playwrigkt.skript.common.models.Reference
import playwrigkt.skript.user.models.UserProfile
import java.util.*

object ChatRoomFixture {

    fun generateChatroom(): ChatRoom = generateChatroom(UUID.randomUUID().toString())

    fun generateChatroom(id: String,
                         users: Set<ChatRoomUser> = emptySet(),
                         publicPermissions: Set<String> = emptySet()): ChatRoom =
            ChatRoom(
                    id = id,
                    name  = "chatroom $id",
                    description = "Generated Chatroom $id",
                    users = users,
                    publicPermissions = publicPermissions)

    fun generateChatroom(chatroomId: String,
                         owner: UserProfile,
                         publicPermissions: Set<String> = emptySet()): ChatRoom=
            generateChatroom(
                    chatroomId,
                    setOf(ChatRoomUser(
                            Reference.empty(owner.id),
                            Reference.empty(chatroomId),
                            allPermissions.map { it.key }.toSet())),
                    publicPermissions)

    fun chatRoomUser(userId: String, chatroomId: String, permissions: Set<String>): ChatRoomUser =
        ChatRoomUser(Reference.empty(userId), Reference.empty(chatroomId), permissions)

    fun chatRoomUser(user: UserProfile, chatroom: ChatRoom, permissions: Set<String>) =
            ChatRoomUser(Reference.defined(user.id, user), Reference.defined(chatroom.id, chatroom), permissions)

    val readPermissions = setOf(
            ChatRoomPermissionKey.Get
    )

    val adminUserPermissions =  setOf(
            ChatRoomPermissionKey.Get,
            ChatRoomPermissionKey.AddUserPermission,
            ChatRoomPermissionKey.RemoveUserPermission
    )

    val allPermissions = setOf(
            ChatRoomPermissionKey.Get,
            ChatRoomPermissionKey.Update,
            ChatRoomPermissionKey.AddPublicPermission,
            ChatRoomPermissionKey.RemovePublicPermission,
            ChatRoomPermissionKey.AddUserPermission,
            ChatRoomPermissionKey.RemoveUserPermission
    )
}