package playwrigkt.skript.chatrooom.models

import playwrigkt.skript.common.models.Reference
import playwrigkt.skript.user.models.UserProfile

data class ChatRoom(
        val id: String,
        val name: String,
        val description: String,
        val users: Set<playwrigkt.skript.chatrooom.models.ChatRoomUser>,
        val publicPermissions: Set<String>
)

data class ChatRoomPermissions(val chatroom: Reference<String, playwrigkt.skript.chatrooom.models.ChatRoom>, val publicPermissions: Set<String>)

data class ChatRoomUser(val user: Reference<String, UserProfile>, val chatroom: Reference<String, playwrigkt.skript.chatrooom.models.ChatRoom>, val permissions: Set<String>)

sealed class ChatRoomPermissionKey {
    val key: String = this.javaClass.simpleName

    object AddUser: playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey()
    object RemoveUser: playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey()
    object AddPublicPermission: playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey()
    object RemovePublicPermission: playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey()
    object AddUserPermission: playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey()
    object RemoveUserPermission: playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey()
    object Update: playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey()
    object Get: playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey()
}

sealed class ChatRoomError: Throwable() {
    object NoUsers: playwrigkt.skript.chatrooom.models.ChatRoomError()
    data class NotFound(val identifier: String): playwrigkt.skript.chatrooom.models.ChatRoomError()
}
