package playwright.skript.chatrooom.models

import playwright.skript.common.models.Reference
import playwright.skript.user.models.UserProfile

data class ChatRoom(
        val id: String,
        val name: String,
        val description: String,
        val users: Set<playwright.skript.chatrooom.models.ChatRoomUser>,
        val publicPermissions: Set<String>
)

data class ChatRoomPermissions(val chatroom: Reference<String, playwright.skript.chatrooom.models.ChatRoom>, val publicPermissions: Set<String>)

data class ChatRoomUser(val user: Reference<String, UserProfile>, val chatroom: Reference<String, playwright.skript.chatrooom.models.ChatRoom>, val permissions: Set<String>)

sealed class ChatRoomPermissionKey {
    val key: String = this.javaClass.simpleName

    object AddUser: playwright.skript.chatrooom.models.ChatRoomPermissionKey()
    object RemoveUser: playwright.skript.chatrooom.models.ChatRoomPermissionKey()
    object AddPublicPermission: playwright.skript.chatrooom.models.ChatRoomPermissionKey()
    object RemovePublicPermission: playwright.skript.chatrooom.models.ChatRoomPermissionKey()
    object AddUserPermission: playwright.skript.chatrooom.models.ChatRoomPermissionKey()
    object RemoveUserPermission: playwright.skript.chatrooom.models.ChatRoomPermissionKey()
    object Update: playwright.skript.chatrooom.models.ChatRoomPermissionKey()
    object Get: playwright.skript.chatrooom.models.ChatRoomPermissionKey()
}

sealed class ChatRoomError: Throwable() {
    object NoUsers: playwright.skript.chatrooom.models.ChatRoomError()
    data class NotFound(val identifier: String): playwright.skript.chatrooom.models.ChatRoomError()
}
