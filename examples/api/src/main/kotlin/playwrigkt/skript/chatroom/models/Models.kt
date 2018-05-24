package playwrigkt.skript.chatroom.models

import playwrigkt.skript.common.models.Reference
import playwrigkt.skript.user.models.UserProfile

data class ChatRoom(
        val id: String,
        val name: String,
        val description: String,
        val users: Set<playwrigkt.skript.chatroom.models.ChatRoomUser>,
        val publicPermissions: Set<String>
)

data class ChatRoomPermissions(val chatroom: Reference<String, playwrigkt.skript.chatroom.models.ChatRoom>, val publicPermissions: Set<String>)

data class ChatRoomUser(val user: Reference<String, UserProfile>, val chatroom: Reference<String, playwrigkt.skript.chatroom.models.ChatRoom>, val permissions: Set<String>)

sealed class ChatRoomPermissionKey {
    val key: String = this.javaClass.simpleName

    object AddPublicPermission: playwrigkt.skript.chatroom.models.ChatRoomPermissionKey()
    object RemovePublicPermission: playwrigkt.skript.chatroom.models.ChatRoomPermissionKey()
    object AddUserPermission: playwrigkt.skript.chatroom.models.ChatRoomPermissionKey()
    object RemoveUserPermission: playwrigkt.skript.chatroom.models.ChatRoomPermissionKey()
    object Update: playwrigkt.skript.chatroom.models.ChatRoomPermissionKey()
    object Get: playwrigkt.skript.chatroom.models.ChatRoomPermissionKey()
}

sealed class ChatRoomError: Throwable() {
    object NoUsers: playwrigkt.skript.chatroom.models.ChatRoomError()
    data class NotFound(val identifier: String): playwrigkt.skript.chatroom.models.ChatRoomError()
}
