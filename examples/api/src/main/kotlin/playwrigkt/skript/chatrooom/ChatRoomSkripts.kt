package playwrigkt.skript.chatrooom
import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.chatrooom.sql.query.*
import playwrigkt.skript.chatrooom.sql.update.*
import playwrigkt.skript.ex.query
import playwrigkt.skript.ex.update
import playwrigkt.skript.troupe.ApplicationTroupe

object ChatRoomSkripts {
    private val onlyIfHasUsers: (playwrigkt.skript.chatrooom.models.ChatRoom) -> Try<playwrigkt.skript.chatrooom.models.ChatRoom> = { if(it.users.isEmpty()) Try.Failure(playwrigkt.skript.chatrooom.models.ChatRoomError.NoUsers) else { Try.Success(it) } }

    val UPDATE_CHAT_ROOM_TRANSACTION: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> =
            authenticate<playwrigkt.skript.chatrooom.models.ChatRoom>()
                    .query(AuthorzeChatRoomUpdate)
                    .map { it.input }
                    .update(UpdateChatRoomFields)
                    .map { it.id }
                    .query(GetChatRoom)

    val GET_CHAT_ROOM_TRANSACTION: Skript<playwrigkt.skript.auth.TokenAndInput<String>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> =
            authenticate<String>()
                    .query(AuthorizedGetChatroom)
                    .map { it.input }
                    .query(GetChatRoom)

    val CREATE_CHAT_ROOM_TRANSACTION: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> =
            authenticate<playwrigkt.skript.chatrooom.models.ChatRoom>()
                    .query(AuthrorizeCreateChatroom)
                    .map { it.input }
                    .mapTry(playwrigkt.skript.chatrooom.ChatRoomSkripts.onlyIfHasUsers)
                    .update(InsertChatRoom)
                    .update(InsertChatRoomUsers)
                    .update(InsertChatRoomPermissions)

    val ADD_PERMISSIONS: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> =
            authenticate<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>()
                    .query(AuthorizeAddPublicPermission)
                    .map { it.input }
                    .update(AddChatRoomPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val REMOVE_PERMISSIONS: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> =
            authenticate<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>()
                    .query(AuthorizeRemovePublicPermission)
                    .map { it.input }
                    .update(DeleteChatRoomPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val ADD_USER_PERMISSIONS: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> =
            authenticate<playwrigkt.skript.chatrooom.models.ChatRoomUser>()
                    .query(AuthorizeAddUserPermission)
                    .map { it.input}
                    .update(InsertChatRoomUserPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val REMOVE_USER_PERMISSIONS: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> =
            authenticate<playwrigkt.skript.chatrooom.models.ChatRoomUser>()
                    .query(AuthorizeRemoveUserPermission)
                    .map { it.input}
                    .update(DeleteChatRoomUserPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)
}




