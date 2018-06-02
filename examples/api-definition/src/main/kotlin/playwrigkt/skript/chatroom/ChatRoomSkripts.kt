package playwrigkt.skript.chatroom
import arrow.core.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.chatroom.sql.query.*
import playwrigkt.skript.chatroom.sql.update.*
import playwrigkt.skript.ex.query
import playwrigkt.skript.ex.update
import playwrigkt.skript.sql.SqlSkript
import playwrigkt.skript.sql.transaction.SqlTransactionSkript
import playwrigkt.skript.troupe.ApplicationTroupe

object ChatRoomSkripts {
    private val onlyIfHasUsers: (playwrigkt.skript.chatroom.models.ChatRoom) -> Try<playwrigkt.skript.chatroom.models.ChatRoom> = { if(it.users.isEmpty()) Try.Failure(playwrigkt.skript.chatroom.models.ChatRoomError.NoUsers) else { Try.Success(it) } }

    val UPDATE_CHAT_ROOM_TRANSACTION: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatroom.models.ChatRoom>, playwrigkt.skript.chatroom.models.ChatRoom, ApplicationTroupe> =
            SqlTransactionSkript.transaction(
                authenticate<playwrigkt.skript.chatroom.models.ChatRoom>()
                        .query(AuthorzeChatRoomUpdate)
                        .map { it.input }
                        .update(UpdateChatRoomFields)
                        .map { it.id }
                        .query(GetChatRoom))

    val GET_CHAT_ROOM_TRANSACTION: Skript<playwrigkt.skript.auth.TokenAndInput<String>, playwrigkt.skript.chatroom.models.ChatRoom, ApplicationTroupe> =
            SqlTransactionSkript.autoCommit(
                    authenticate<String>()
                            .query(AuthorizedGetChatroom)
                            .map { it.input }
                            .query(GetChatRoom))

    val CREATE_CHAT_ROOM_TRANSACTION: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatroom.models.ChatRoom>, playwrigkt.skript.chatroom.models.ChatRoom, ApplicationTroupe> =
            SqlTransactionSkript.transaction(
                    authenticate<playwrigkt.skript.chatroom.models.ChatRoom>()
                            .query(AuthrorizeCreateChatroom)
                            .map { it.input }
                            .mapTry(playwrigkt.skript.chatroom.ChatRoomSkripts.onlyIfHasUsers)
                            .update(InsertChatRoom)
                            .whenTrue(
                                    control = Skript.map { it.users.filter { it.permissions.isNotEmpty() }.isNotEmpty() },
                                    doOptionally = Skript.Wrapped(SqlSkript.update(InsertChatRoomUsers))
                            )
                            .whenTrue(
                                    control = Skript.map { it.publicPermissions.isNotEmpty() },
                                    doOptionally = Skript.Wrapped(SqlSkript.update(InsertChatRoomPermissions))
                            ))

    val ADD_PERMISSIONS: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatroom.models.ChatRoomPermissions>, playwrigkt.skript.chatroom.models.ChatRoom, ApplicationTroupe> =
            SqlTransactionSkript.transaction(
                    authenticate<playwrigkt.skript.chatroom.models.ChatRoomPermissions>()
                            .query(AuthorizeAddPublicPermission)
                            .map { it.input }
                            .whenTrue(
                                    control = Skript.map { it.publicPermissions.isNotEmpty() },
                                    doOptionally = Skript.Wrapped(SqlSkript.update(AddChatRoomPermissions))
                            )
                            .map { it.chatroom.id }
                            .query(GetChatRoom))

    val REMOVE_PERMISSIONS: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatroom.models.ChatRoomPermissions>, playwrigkt.skript.chatroom.models.ChatRoom, ApplicationTroupe> =
            SqlTransactionSkript.transaction(
                    authenticate<playwrigkt.skript.chatroom.models.ChatRoomPermissions>()
                            .query(AuthorizeRemovePublicPermission)
                            .map { it.input }
                            .whenTrue(
                                    control = Skript.map { it.publicPermissions.isNotEmpty() },
                                    doOptionally = Skript.Wrapped(SqlSkript.update(DeleteChatRoomPermissions))
                            )
                            .map { it.chatroom.id }
                            .query(GetChatRoom))

    val ADD_USER_PERMISSIONS: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatroom.models.ChatRoomUser>, playwrigkt.skript.chatroom.models.ChatRoom, ApplicationTroupe> =
            SqlTransactionSkript.transaction(
                    authenticate<playwrigkt.skript.chatroom.models.ChatRoomUser>()
                            .query(AuthorizeAddUserPermission)
                            .map { it.input}
                            .whenTrue(
                                    control = Skript.map { it.permissions.isNotEmpty() },
                                    doOptionally = Skript.Wrapped(SqlSkript.update(InsertChatRoomUserPermissions))
                            )
                            .map { it.chatroom.id }
                            .query(GetChatRoom))

    val REMOVE_USER_PERMISSIONS: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatroom.models.ChatRoomUser>, playwrigkt.skript.chatroom.models.ChatRoom, ApplicationTroupe> =
            SqlTransactionSkript.transaction(
                    authenticate<playwrigkt.skript.chatroom.models.ChatRoomUser>()
                            .query(AuthorizeRemoveUserPermission)
                            .map { it.input}
                            .whenTrue(
                                    control = Skript.map { it.permissions.isNotEmpty() },
                                    doOptionally = Skript.Wrapped(SqlSkript.update(DeleteChatRoomUserPermissions))
                            )
                            .map { it.chatroom.id }
                            .query(GetChatRoom))
}




