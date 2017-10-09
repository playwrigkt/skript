package dev.yn.playground.chatrooom.sql
import dev.yn.playground.auth.TokenAndInput
import dev.yn.playground.chatrooom.models.ChatRoom
import dev.yn.playground.chatrooom.models.ChatRoomError
import dev.yn.playground.chatrooom.models.ChatRoomPermissions
import dev.yn.playground.chatrooom.models.ChatRoomUser
import dev.yn.playground.chatrooom.sql.query.*
import dev.yn.playground.chatrooom.sql.update.*
import dev.yn.playground.sql.*
import dev.yn.playground.common.ApplicationContextProvider
import org.funktionale.tries.Try

object ChatRoomTransactions {
    private val onlyIfHasUsers: (ChatRoom) -> Try<ChatRoom> = { if(it.users.isEmpty()) Try.Failure(ChatRoomError.NoUsers) else { Try.Success(it) } }

    val addUserTransaction: UnpreparedSQLAction<TokenAndInput<ChatRoomUser>, ChatRoom, ApplicationContextProvider> =
            authenticate<ChatRoomUser>()
                    .query(AuthorizeChatRoomAddUser)
                    .map { it.input }
                    .update(InsertChatRoomUserPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val removeUserTransaction: UnpreparedSQLAction<TokenAndInput<ChatRoomUser>, ChatRoom, ApplicationContextProvider> =
            authenticate<ChatRoomUser>()
                    .query(AuthorizeChatRoomRemoveUser)
                    .map { it.input }
                    .update(DeleteChatRoomUserPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val updateChatRoomTransaction: UnpreparedSQLAction<TokenAndInput<ChatRoom>, ChatRoom, ApplicationContextProvider> =
            authenticate<ChatRoom>()
                    .query(AuthorzeChatRoomUpdate)
                    .map { it.input }
                    .update(UpdateChatRoomFields)
                    .map { it.id }
                    .query(GetChatRoom)

    val getChatRoomTransaction: UnpreparedSQLAction<TokenAndInput<String>, ChatRoom, ApplicationContextProvider> =
            authenticate<String>()
                    .query(AuthorizedGetChatroom)
                    .map { it.input }
                    .query(GetChatRoom)

    val createChatRoomTransaction: UnpreparedSQLAction<TokenAndInput<ChatRoom>, ChatRoom, ApplicationContextProvider> =
            authenticate<ChatRoom>()
                    .query(AuthrorizeCreateChatroom)
                    .map { it.input }
                    .mapTry(onlyIfHasUsers)
                    .update(InsertChatRoom)
                    .update(InsertChatRoomUsers)
                    .update(InsertChatRoomPermissions)

    val addPermissions: UnpreparedSQLAction<TokenAndInput<ChatRoomPermissions>, ChatRoom, ApplicationContextProvider> =
            authenticate<ChatRoomPermissions>()
                    .query(AuthorizeAddPublicPermission)
                    .map { it.input }
                    .update(AddChatRoomPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val removePermissions: UnpreparedSQLAction<TokenAndInput<ChatRoomPermissions>, ChatRoom, ApplicationContextProvider> =
            authenticate<ChatRoomPermissions>()
                    .query(AuthorizeRemovePublicPermission)
                    .map { it.input }
                    .update(DeleteChatRoomPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val addUserPermissions: UnpreparedSQLAction<TokenAndInput<ChatRoomUser>, ChatRoom, ApplicationContextProvider> =
            authenticate<ChatRoomUser>()
                    .query(AuthorizeAddUserPermission)
                    .map { it.input}
                    .update(InsertChatRoomUserPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)

    val removeUserPermissions: UnpreparedSQLAction<TokenAndInput<ChatRoomUser>, ChatRoom, ApplicationContextProvider> =
            authenticate<ChatRoomUser>()
                    .query(AuthorizeRemoveUserPermission)
                    .map { it.input}
                    .update(DeleteChatRoomUserPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom)
}




