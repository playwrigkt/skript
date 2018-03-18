package playwright.skript.chatrooom
import org.funktionale.tries.Try
import playwright.skript.Skript
import playwright.skript.chatrooom.sql.query.*
import playwright.skript.chatrooom.sql.update.*
import playwright.skript.common.ApplicationStage
import playwright.skript.ex.query
import playwright.skript.ex.update

object ChatRoomSkripts {
    private val onlyIfHasUsers: (playwright.skript.chatrooom.models.ChatRoom) -> Try<playwright.skript.chatrooom.models.ChatRoom> = { if(it.users.isEmpty()) Try.Failure(playwright.skript.chatrooom.models.ChatRoomError.NoUsers) else { Try.Success(it) } }

    val ADD_USER_TRANSACTION: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> =
            authenticate<playwright.skript.chatrooom.models.ChatRoomUser>()
                    .query(AuthorizeChatRoomAddUser)
                    .map { it.input }
                    .update(InsertChatRoomUserPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val REMOVE_USER_TRANSACTION: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> =
            authenticate<playwright.skript.chatrooom.models.ChatRoomUser>()
                    .query(AuthorizeChatRoomRemoveUser)
                    .map { it.input }
                    .update(DeleteChatRoomUserPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val UPDATE_CHAT_ROOM_TRANSACTION: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoom>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> =
            authenticate<playwright.skript.chatrooom.models.ChatRoom>()
                    .query(AuthorzeChatRoomUpdate)
                    .map { it.input }
                    .update(UpdateChatRoomFields)
                    .map { it.id }
                    .query(GetChatRoom)

    val GET_CHAT_ROOM_TRANSACTION: Skript<playwright.skript.auth.TokenAndInput<String>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> =
            authenticate<String>()
                    .query(AuthorizedGetChatroom)
                    .map { it.input }
                    .query(GetChatRoom)

    val CREATE_CHAT_ROOM_TRANSACTION: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoom>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> =
            authenticate<playwright.skript.chatrooom.models.ChatRoom>()
                    .query(AuthrorizeCreateChatroom)
                    .map { it.input }
                    .mapTry(playwright.skript.chatrooom.ChatRoomSkripts.onlyIfHasUsers)
                    .update(InsertChatRoom)
                    .update(InsertChatRoomUsers)
                    .update(InsertChatRoomPermissions)

    val ADD_PERMISSIONS: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoomPermissions>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> =
            authenticate<playwright.skript.chatrooom.models.ChatRoomPermissions>()
                    .query(AuthorizeAddPublicPermission)
                    .map { it.input }
                    .update(AddChatRoomPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val REMOVE_PERMISSIONS: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoomPermissions>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> =
            authenticate<playwright.skript.chatrooom.models.ChatRoomPermissions>()
                    .query(AuthorizeRemovePublicPermission)
                    .map { it.input }
                    .update(DeleteChatRoomPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val ADD_USER_PERMISSIONS: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> =
            authenticate<playwright.skript.chatrooom.models.ChatRoomUser>()
                    .query(AuthorizeAddUserPermission)
                    .map { it.input}
                    .update(InsertChatRoomUserPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val REMOVE_USER_PERMISSIONS: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> =
            authenticate<playwright.skript.chatrooom.models.ChatRoomUser>()
                    .query(AuthorizeRemoveUserPermission)
                    .map { it.input}
                    .update(DeleteChatRoomUserPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)
}




