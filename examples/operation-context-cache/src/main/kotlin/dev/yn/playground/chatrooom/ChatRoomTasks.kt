package dev.yn.playground.chatrooom
import dev.yn.playground.Task
import dev.yn.playground.ex.andThen
import dev.yn.playground.auth.context.UserSessionCache
import dev.yn.playground.auth.AuthTasks
import dev.yn.playground.chatrooom.cache.ChatroomCacheTasks
import dev.yn.playground.chatrooom.context.ChatroomOperationCache
import dev.yn.playground.chatrooom.models.*
import dev.yn.playground.chatrooom.sql.query.GetChatRoom
import dev.yn.playground.chatrooom.sql.update.*
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.ex.query
import dev.yn.playground.ex.update
import dev.yn.playground.user.models.UserError
import devyn.playground.sql.task.SQLTransactionTask
import org.funktionale.option.firstOption
import org.funktionale.option.getOrElse
import org.funktionale.option.orElse
import org.funktionale.tries.Try

object ChatRoomTasks {
    private val onlyIfHasUsers: (ChatRoom) -> Try<ChatRoom> = { if(it.users.isEmpty()) Try.Failure(ChatRoomError.NoUsers) else { Try.Success(it) } }

    val addUserTask: Task<ChatRoomUser, ChatRoom, ApplicationContext<ChatroomOperationCache>> =
            SQLTransactionTask.transaction(
                    AuthTasks
                            .validate<ChatRoomUser, ChatroomOperationCache>()
                            .andThen(ChatroomCacheTasks.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.AddUser))
                            .update(InsertChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))
    val removeUserTask: Task<ChatRoomUser, ChatRoom, ApplicationContext<ChatroomOperationCache>> =
            SQLTransactionTask.transaction(
                    AuthTasks
                            .validate<ChatRoomUser, ChatroomOperationCache>()
                            .andThen(ChatroomCacheTasks.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.RemoveUser))
                            .update(DeleteChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))
    val addUserPermissionsTask: Task<ChatRoomUser, ChatRoom, ApplicationContext<ChatroomOperationCache>> =
            SQLTransactionTask.transaction(
                    AuthTasks.validate<ChatRoomUser, ChatroomOperationCache>()
                            .andThen(ChatroomCacheTasks.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.AddUserPermission))
                            .update(InsertChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))

    val removeUserPermissionsTask: Task<ChatRoomUser, ChatRoom, ApplicationContext<ChatroomOperationCache>> =
            SQLTransactionTask.transaction(
                    AuthTasks.validate<ChatRoomUser, ChatroomOperationCache>()
                            .andThen(ChatroomCacheTasks.hydrateExistingChatroom<ChatRoomUser>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.RemoveUserPermission))
                            .update(DeleteChatRoomUserPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))

    val addPublicPermissionTask: Task<ChatRoomPermissions, ChatRoom, ApplicationContext<ChatroomOperationCache>> =
            SQLTransactionTask.transaction(
                    AuthTasks.validate<ChatRoomPermissions, ChatroomOperationCache>()
                            .andThen(ChatroomCacheTasks.hydrateExistingChatroom<ChatRoomPermissions>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.AddPublicPermission))
                            .update(AddChatRoomPermissions)
                            .map { it.chatroom.id }
                            .query(GetChatRoom))
    val removePublicPermissionTask: Task<ChatRoomPermissions, ChatRoom, ApplicationContext<ChatroomOperationCache>> =
            SQLTransactionTask.transaction(AuthTasks.validate<ChatRoomPermissions, ChatroomOperationCache>()
                    .andThen(ChatroomCacheTasks.hydrateExistingChatroom<ChatRoomPermissions>())
                    .andThen(authorizeUser(ChatRoomPermissionKey.RemovePublicPermission))
                    .update(DeleteChatRoomPermissions)
                    .map { it.chatroom.id }
                    .query(GetChatRoom))

    val createChatRoomTask: Task<ChatRoom, ChatRoom, ApplicationContext<UserSessionCache>> =
            SQLTransactionTask.transaction(
                    AuthTasks.validate<ChatRoom, UserSessionCache>()
                            .mapTry(onlyIfHasUsers)
                            .update(InsertChatRoom)
                            .update(InsertChatRoomUsers)
                            .update(InsertChatRoomPermissions))

    val updateChatRoomTask: Task<ChatRoom, ChatRoom, ApplicationContext<ChatroomOperationCache>> =
            SQLTransactionTask.transaction(
                    AuthTasks.validate<ChatRoom, ChatroomOperationCache>()
                            .andThen(ChatroomCacheTasks.hydrateExistingChatroom<ChatRoom>())
                            .andThen(authorizeUser(ChatRoomPermissionKey.Update))
                            .update(UpdateChatRoomFields)
                            .map { it.id }
                            .query(GetChatRoom))
    val getChatroomTask: Task<String, ChatRoom, ApplicationContext<ChatroomOperationCache>> =
            SQLTransactionTask.transaction(
                    AuthTasks.validate<String, ChatroomOperationCache>()
                            .andThen(ChatroomCacheTasks.hydrateExistingChatroomById())
                            .andThen(authorizeUser(ChatRoomPermissionKey.Get))
                            .mapTryWithContext(this::loadUserFromCache))









    private fun <I> authorizeUser(chatRoomPermission: ChatRoomPermissionKey): Task<I, I, ApplicationContext<ChatroomOperationCache>> = Task.mapTryWithContext { i, context ->
        context.cache.getChatroom()
                .filter { it.publicPermissions.contains(chatRoomPermission.key) }
                .orElse { context.cache
                        .getChatroom()
                        .flatMap { chatRoom -> context.cache
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

    private fun loadUserFromCache(id: String, context: ApplicationContext<ChatroomOperationCache>): Try<ChatRoom> {
        return context.cache.getChatroom()
                .map { Try.Success(it) }
                .getOrElse { Try.Failure<ChatRoom>(ChatRoomError.NotFound(id)) }
    }

}




