package dev.yn.playground.chatrooom
import dev.yn.playground.Skript
import dev.yn.playground.auth.TokenAndInput
import dev.yn.playground.chatrooom.models.ChatRoom
import dev.yn.playground.chatrooom.models.ChatRoomError
import dev.yn.playground.chatrooom.models.ChatRoomPermissions
import dev.yn.playground.chatrooom.models.ChatRoomUser
import dev.yn.playground.chatrooom.sql.query.*
import dev.yn.playground.chatrooom.sql.update.*
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.ex.query
import dev.yn.playground.ex.update
import org.funktionale.tries.Try

object ChatRoomSkripts {
    private val onlyIfHasUsers: (ChatRoom) -> Try<ChatRoom> = { if(it.users.isEmpty()) Try.Failure(ChatRoomError.NoUsers) else { Try.Success(it) } }

    val ADD_USER_TRANSACTION: Skript<TokenAndInput<ChatRoomUser>, ChatRoom, ApplicationContext> =
            authenticate<ChatRoomUser>()
                    .query(AuthorizeChatRoomAddUser)
                    .map { it.input }
                    .update(InsertChatRoomUserPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val REMOVE_USER_TRANSACTION: Skript<TokenAndInput<ChatRoomUser>, ChatRoom, ApplicationContext> =
            authenticate<ChatRoomUser>()
                    .query(AuthorizeChatRoomRemoveUser)
                    .map { it.input }
                    .update(DeleteChatRoomUserPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val UPDATE_CHAT_ROOM_TRANSACTION: Skript<TokenAndInput<ChatRoom>, ChatRoom, ApplicationContext> =
            authenticate<ChatRoom>()
                    .query(AuthorzeChatRoomUpdate)
                    .map { it.input }
                    .update(UpdateChatRoomFields)
                    .map { it.id }
                    .query(GetChatRoom)

    val GET_CHAT_ROOM_TRANSACTION: Skript<TokenAndInput<String>, ChatRoom, ApplicationContext> =
            authenticate<String>()
                    .query(AuthorizedGetChatroom)
                    .map { it.input }
                    .query(GetChatRoom)

    val CREATE_CHAT_ROOM_TRANSACTION: Skript<TokenAndInput<ChatRoom>, ChatRoom, ApplicationContext> =
            authenticate<ChatRoom>()
                    .query(AuthrorizeCreateChatroom)
                    .map { it.input }
                    .mapTry(onlyIfHasUsers)
                    .update(InsertChatRoom)
                    .update(InsertChatRoomUsers)
                    .update(InsertChatRoomPermissions)

    val ADD_PERMISSIONS: Skript<TokenAndInput<ChatRoomPermissions>, ChatRoom, ApplicationContext> =
            authenticate<ChatRoomPermissions>()
                    .query(AuthorizeAddPublicPermission)
                    .map { it.input }
                    .update(AddChatRoomPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val REMOVE_PERMISSIONS: Skript<TokenAndInput<ChatRoomPermissions>, ChatRoom, ApplicationContext> =
            authenticate<ChatRoomPermissions>()
                    .query(AuthorizeRemovePublicPermission)
                    .map { it.input }
                    .update(DeleteChatRoomPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val ADD_USER_PERMISSIONS: Skript<TokenAndInput<ChatRoomUser>, ChatRoom, ApplicationContext> =
            authenticate<ChatRoomUser>()
                    .query(AuthorizeAddUserPermission)
                    .map { it.input}
                    .update(InsertChatRoomUserPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val REMOVE_USER_PERMISSIONS: Skript<TokenAndInput<ChatRoomUser>, ChatRoom, ApplicationContext> =
            authenticate<ChatRoomUser>()
                    .query(AuthorizeRemoveUserPermission)
                    .map { it.input}
                    .update(DeleteChatRoomUserPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)
}




