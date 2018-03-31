package playwrigkt.skript.chatrooom
import org.funktionale.option.firstOption
import org.funktionale.option.getOrElse
import org.funktionale.option.orElse
import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.auth.AuthSkripts
import playwrigkt.skript.auth.props.UserSessionStageProps
import playwrigkt.skript.chatrooom.models.*
import playwrigkt.skript.chatrooom.props.ChatroomStageProps
import playwrigkt.skript.chatrooom.props.ChatroomPropsSkripts
import playwrigkt.skript.chatrooom.sql.query.GetChatRoom
import playwrigkt.skript.chatrooom.sql.update.*
import playwrigkt.skript.common.ApplicationStage
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.ex.query
import playwrigkt.skript.ex.update
import playwrigkt.skript.sql.transaction.SQLTransactionSkript
import playwrigkt.skript.user.models.UserError

object ChatRoomSkripts {
    private val onlyIfHasUsers: (ChatRoom) -> Try<ChatRoom> = { if(it.users.isEmpty()) Try.Failure(ChatRoomError.NoUsers) else { Try.Success(it) } }

    val ADD_USER_SKRIPT: Skript<ChatRoomUser, ChatRoom, ApplicationStage<ChatroomStageProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts
                            .validate<ChatRoomUser, ChatroomStageProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.AddUser))
                            .update(InsertChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))

    val REMOVE_USER_SKRIPT: Skript<ChatRoomUser, ChatRoom, ApplicationStage<ChatroomStageProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts
                            .validate<ChatRoomUser, ChatroomStageProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.RemoveUser))
                            .update(DeleteChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))
    val ADD_USER_PERMISSIONS_SKRIPT: Skript<ChatRoomUser, ChatRoom, ApplicationStage<ChatroomStageProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoomUser, ChatroomStageProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.AddUserPermission))
                            .update(InsertChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))

    val REMOVE_USER_PERMISSIONS_SKRIPT: Skript<ChatRoomUser, ChatRoom, ApplicationStage<ChatroomStageProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoomUser, ChatroomStageProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.RemoveUserPermission))
                            .update(DeleteChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))

    val ADD_PUBLIC_PERMISSION_SKRIPT: Skript<ChatRoomPermissions, ChatRoom, ApplicationStage<ChatroomStageProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoomPermissions, ChatroomStageProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomPermissions>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.AddPublicPermission))
                            .update(AddChatRoomPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))
    val REMOVE_PUBLIC_PERMISSION_SKRIPT: Skript<ChatRoomPermissions, ChatRoom, ApplicationStage<ChatroomStageProps>> =
            SQLTransactionSkript.transaction(AuthSkripts.validate<ChatRoomPermissions, ChatroomStageProps>()
                    .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomPermissions>())
                    .andThen(authorizeUser(ChatRoomPermissionKey.RemovePublicPermission))
                    .update(DeleteChatRoomPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom))

    val CREATE_CHAT_ROOM_SKRIPT: Skript<ChatRoom, ChatRoom, ApplicationStage<UserSessionStageProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoom, UserSessionStageProps>()
                            .mapTry(onlyIfHasUsers)
                            .update(InsertChatRoom)
                            .update(InsertChatRoomUsers)
                            .update(InsertChatRoomPermissions))

    val UPDATE_CHAT_ROOM_SKRIPT: Skript<ChatRoom, ChatRoom, ApplicationStage<ChatroomStageProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoom, ChatroomStageProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoom>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.Update))
                            .update(UpdateChatRoomFields)
                            .map { it.id }
                            .query(GetChatRoom))

    val GET_CHATROOM_SKRIPT: Skript<String, ChatRoom, ApplicationStage<ChatroomStageProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<String, ChatroomStageProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroomById())
                            .andThen(authorizeUser(ChatRoomPermissionKey.Get))
                            .mapTryWithStage(this::loadUserFromCache))









    private fun <I> authorizeUser(chatRoomPermission: ChatRoomPermissionKey): Skript<I, I, ApplicationStage<ChatroomStageProps>> = Skript.mapTryWithStage { i, stage ->
        stage.getStageProps().getChatroom()
                .filter { it.publicPermissions.contains(chatRoomPermission.key) }
                .orElse { stage.getStageProps()
                        .getChatroom()
                        .flatMap { chatRoom -> stage.getStageProps()
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

    private fun loadUserFromCache(id: String, stage: ApplicationStage<ChatroomStageProps>): Try<ChatRoom> {
        return stage.getStageProps().getChatroom()
                .map { Try.Success(it) }
                .getOrElse { Try.Failure<ChatRoom>(ChatRoomError.NotFound(id)) }
    }

}




