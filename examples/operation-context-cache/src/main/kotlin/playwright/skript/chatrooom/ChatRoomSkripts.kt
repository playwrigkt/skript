package playwright.skript.chatrooom
import org.funktionale.option.firstOption
import org.funktionale.option.getOrElse
import org.funktionale.option.orElse
import org.funktionale.tries.Try
import playwright.skript.Skript
import playwright.skript.auth.AuthSkripts
import playwright.skript.auth.props.UserSessionProps
import playwright.skript.chatrooom.models.*
import playwright.skript.chatrooom.props.ChatroomOperationProps
import playwright.skript.chatrooom.props.ChatroomPropsSkripts
import playwright.skript.chatrooom.sql.query.GetChatRoom
import playwright.skript.chatrooom.sql.update.*
import playwright.skript.common.ApplicationStage
import playwright.skript.ex.andThen
import playwright.skript.ex.query
import playwright.skript.ex.update
import playwright.skript.sql.transaction.SQLTransactionSkript
import playwright.skript.user.models.UserError

object ChatRoomSkripts {
    private val onlyIfHasUsers: (ChatRoom) -> Try<ChatRoom> = { if(it.users.isEmpty()) Try.Failure(ChatRoomError.NoUsers) else { Try.Success(it) } }

    val ADD_USER_SKRIPT: Skript<ChatRoomUser, ChatRoom, ApplicationStage<ChatroomOperationProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts
                            .validate<ChatRoomUser, ChatroomOperationProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.AddUser))
                            .update(InsertChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))
    val REMOVE_USER_SKRIPT: Skript<ChatRoomUser, ChatRoom, ApplicationStage<ChatroomOperationProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts
                            .validate<ChatRoomUser, ChatroomOperationProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.RemoveUser))
                            .update(DeleteChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))
    val ADD_USER_PERMISSIONS_SKRIPT: Skript<ChatRoomUser, ChatRoom, ApplicationStage<ChatroomOperationProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoomUser, ChatroomOperationProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.AddUserPermission))
                            .update(InsertChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))

    val REMOVE_USER_PERMISSIONS_SKRIPT: Skript<ChatRoomUser, ChatRoom, ApplicationStage<ChatroomOperationProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoomUser, ChatroomOperationProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.RemoveUserPermission))
                            .update(DeleteChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))

    val ADD_PUBLIC_PERMISSION_SKRIPT: Skript<ChatRoomPermissions, ChatRoom, ApplicationStage<ChatroomOperationProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoomPermissions, ChatroomOperationProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomPermissions>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.AddPublicPermission))
                            .update(AddChatRoomPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))
    val REMOVE_PUBLIC_PERMISSION_SKRIPT: Skript<ChatRoomPermissions, ChatRoom, ApplicationStage<ChatroomOperationProps>> =
            SQLTransactionSkript.transaction(AuthSkripts.validate<ChatRoomPermissions, ChatroomOperationProps>()
                    .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoomPermissions>())
                    .andThen(authorizeUser(ChatRoomPermissionKey.RemovePublicPermission))
                    .update(DeleteChatRoomPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom))

    val CREATE_CHAT_ROOM_SKRIPT: Skript<ChatRoom, ChatRoom, ApplicationStage<UserSessionProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoom, UserSessionProps>()
                            .mapTry(onlyIfHasUsers)
                            .update(InsertChatRoom)
                            .update(InsertChatRoomUsers)
                            .update(InsertChatRoomPermissions))

    val UPDATE_CHAT_ROOM_SKRIPT: Skript<ChatRoom, ChatRoom, ApplicationStage<ChatroomOperationProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<ChatRoom, ChatroomOperationProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoom>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.Update))
                            .update(UpdateChatRoomFields)
                            .map { it.id }
                            .query(GetChatRoom))
    val GET_CHATROOM_SKRIPT: Skript<String, ChatRoom, ApplicationStage<ChatroomOperationProps>> =
            SQLTransactionSkript.transaction(
                    AuthSkripts.validate<String, ChatroomOperationProps>()
                            .andThen(ChatroomPropsSkripts.hydrateExistingChatroomById())
                            .andThen(authorizeUser(ChatRoomPermissionKey.Get))
                            .mapTryWithContext(this::loadUserFromCache))









    private fun <I> authorizeUser(chatRoomPermission: ChatRoomPermissionKey): Skript<I, I, ApplicationStage<ChatroomOperationProps>> = Skript.mapTryWithContext { i, context ->
        context.getStageProps().getChatroom()
                .filter { it.publicPermissions.contains(chatRoomPermission.key) }
                .orElse { context.getStageProps()
                        .getChatroom()
                        .flatMap { chatRoom -> context.getStageProps()
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

    private fun loadUserFromCache(id: String, context: ApplicationStage<ChatroomOperationProps>): Try<ChatRoom> {
        return context.getStageProps().getChatroom()
                .map { Try.Success(it) }
                .getOrElse { Try.Failure<ChatRoom>(ChatRoomError.NotFound(id)) }
    }

}




