package playwrigkt.skript.chatrooom
import org.funktionale.option.firstOption
import org.funktionale.option.getOrElse
import org.funktionale.option.orElse
import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.auth.AuthSkripts
import playwrigkt.skript.auth.props.UserSessionTroupeProps
import playwrigkt.skript.chatrooom.models.*
import playwrigkt.skript.chatrooom.props.ChatroomPropsSkripts
import playwrigkt.skript.chatrooom.props.ChatroomTroupeProps
import playwrigkt.skript.chatrooom.sql.query.GetChatRoom
import playwrigkt.skript.chatrooom.sql.update.*
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.ex.query
import playwrigkt.skript.ex.update
import playwrigkt.skript.sql.transaction.SQLTransactionSkript
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.models.UserError

object ChatRoomSkripts {
    private val onlyIfHasUsers: (ChatRoom) -> Try<ChatRoom> = { if(it.users.isEmpty()) Try.Failure(ChatRoomError.NoUsers) else { Try.Success(it) } }

    val ADD_USER_SKRIPT: Skript<ChatRoomUser, ChatRoom, ApplicationTroupe<ChatroomTroupeProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts
                            .validate<ChatRoomUser, ChatroomTroupeProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.AddUser))
                            .update(InsertChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))

    val REMOVE_USER_SKRIPT: Skript<ChatRoomUser, ChatRoom, ApplicationTroupe<ChatroomTroupeProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts
                            .validate<ChatRoomUser, ChatroomTroupeProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.RemoveUser))
                            .update(DeleteChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))
    val ADD_USER_PERMISSIONS_SKRIPT: Skript<ChatRoomUser, ChatRoom, ApplicationTroupe<ChatroomTroupeProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoomUser, ChatroomTroupeProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.AddUserPermission))
                            .update(InsertChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))

    val REMOVE_USER_PERMISSIONS_SKRIPT: Skript<ChatRoomUser, ChatRoom, ApplicationTroupe<ChatroomTroupeProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoomUser, ChatroomTroupeProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.RemoveUserPermission))
                            .update(DeleteChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))

    val ADD_PUBLIC_PERMISSION_SKRIPT: Skript<ChatRoomPermissions, ChatRoom, ApplicationTroupe<ChatroomTroupeProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoomPermissions, ChatroomTroupeProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomPermissions>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.AddPublicPermission))
                            .update(AddChatRoomPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))
    val REMOVE_PUBLIC_PERMISSION_SKRIPT: Skript<ChatRoomPermissions, ChatRoom, ApplicationTroupe<ChatroomTroupeProps>> =
            SQLTransactionSkript.transaction(AuthSkripts.validate<ChatRoomPermissions, ChatroomTroupeProps>()
                    .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomPermissions>())
                    .andThen(authorizeUser(ChatRoomPermissionKey.RemovePublicPermission))
                    .update(DeleteChatRoomPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom))

    val CREATE_CHAT_ROOM_SKRIPT: Skript<ChatRoom, ChatRoom, ApplicationTroupe<UserSessionTroupeProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoom, UserSessionTroupeProps>()
                            .mapTry(onlyIfHasUsers)
                            .update(InsertChatRoom)
                            .update(InsertChatRoomUsers)
                            .update(InsertChatRoomPermissions))

    val UPDATE_CHAT_ROOM_SKRIPT: Skript<ChatRoom, ChatRoom, ApplicationTroupe<ChatroomTroupeProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoom, ChatroomTroupeProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoom>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.Update))
                            .update(UpdateChatRoomFields)
                            .map { it.id }
                            .query(GetChatRoom))

    val GET_CHATROOM_SKRIPT: Skript<String, ChatRoom, ApplicationTroupe<ChatroomTroupeProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<String, ChatroomTroupeProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroomById())
                            .andThen(authorizeUser(ChatRoomPermissionKey.Get))
                            .mapTryWithTroupe(this::loadUserFromCache))









    private fun <I> authorizeUser(chatRoomPermission: ChatRoomPermissionKey): Skript<I, I, ApplicationTroupe<ChatroomTroupeProps>> = Skript.mapTryWithTroupe { i, stage ->
        stage.getTroupeProps().getChatroom()
                .filter { it.publicPermissions.contains(chatRoomPermission.key) }
                .orElse { stage.getTroupeProps()
                        .getChatroom()
                        .flatMap { chatRoom -> stage.getTroupeProps()
                                .getUserSession()
                                .flatMap { session -> chatRoom.users
                                        .firstOption {
                                            it.user.id == session.userId
                                                    && it.permissions.contains(chatRoomPermission.key)
                                        }
                                }
                        }
                }
                .map { Try.Success(i) }
                .getOrElse { Try.Failure<I>(UserError.AuthorizationFailed) }
    }

    private fun loadUserFromCache(id: String, stage: ApplicationTroupe<ChatroomTroupeProps>): Try<ChatRoom> {
        return stage.getTroupeProps().getChatroom()
                .map { Try.Success(it) }
                .getOrElse { Try.Failure<ChatRoom>(ChatRoomError.NotFound(id)) }
    }

}




