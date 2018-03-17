package dev.yn.playground.chatroom

import dev.yn.playground.Skript
import dev.yn.playground.auth.TokenAndInput
import dev.yn.playground.chatrooom.models.ChatRoom
import dev.yn.playground.chatrooom.models.ChatRoomPermissionKey
import dev.yn.playground.chatrooom.models.ChatRoomPermissions
import dev.yn.playground.chatrooom.models.ChatRoomUser
import dev.yn.playground.chatrooom.sql.ChatRoomSchema
import dev.yn.playground.chatrooom.ChatRoomSkripts
import dev.yn.playground.chatrooom.sql.query.authorizeChatroomSelectStatement
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.common.models.Reference
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.sql.SQLCommand
import dev.yn.playground.sql.SQLError
import dev.yn.playground.user.UserFixture
import dev.yn.playground.user.UserService
import dev.yn.playground.user.extensions.schema.dropUserSchema
import dev.yn.playground.user.extensions.schema.initUserSchema
import dev.yn.playground.user.models.UserError
import dev.yn.playground.user.models.UserNameAndPassword
import dev.yn.playground.sql.transaction.SQLTransactionSkript
import io.kotlintest.Spec
import io.kotlintest.matchers.fail
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import org.slf4j.LoggerFactory

abstract class ChatroomTransactionsSpec : StringSpec() {

    val LOG = LoggerFactory.getLogger(this.javaClass)

    companion object {
        val CREATE_CHAT_ROOM: Skript<TokenAndInput<ChatRoom>, ChatRoom, ApplicationContext> = SQLTransactionSkript.transaction(ChatRoomSkripts.CREATE_CHAT_ROOM_TRANSACTION)
        val GET_CHAT_ROOM: Skript<TokenAndInput<String>, ChatRoom, ApplicationContext> = SQLTransactionSkript.transaction(ChatRoomSkripts.GET_CHAT_ROOM_TRANSACTION)
        val ADD_USER: Skript<TokenAndInput<ChatRoomUser>, ChatRoom, ApplicationContext> = SQLTransactionSkript.transaction(ChatRoomSkripts.ADD_USER_TRANSACTION)
        val DELETE_USER: Skript<TokenAndInput<ChatRoomUser>, ChatRoom, ApplicationContext> = SQLTransactionSkript.transaction(ChatRoomSkripts.REMOVE_USER_TRANSACTION)
        val ADD_PUBLIC_PERMISSIONS: Skript<TokenAndInput<ChatRoomPermissions>, ChatRoom, ApplicationContext> = SQLTransactionSkript.transaction(ChatRoomSkripts.ADD_PERMISSIONS)
        val REMOVE_PUBLIC_PERMISSIONS: Skript<TokenAndInput<ChatRoomPermissions>, ChatRoom, ApplicationContext> = SQLTransactionSkript.transaction(ChatRoomSkripts.REMOVE_PERMISSIONS)
        val UPDATE_CHAT_ROOM: Skript<TokenAndInput<ChatRoom>, ChatRoom, ApplicationContext> = SQLTransactionSkript.transaction(ChatRoomSkripts.UPDATE_CHAT_ROOM_TRANSACTION)
        val ADD_USER_PERMISSION: Skript<TokenAndInput<ChatRoomUser>, ChatRoom, ApplicationContext> = SQLTransactionSkript.transaction(ChatRoomSkripts.ADD_USER_PERMISSIONS)
        val REMOVE_USER_PERMISSION: Skript<TokenAndInput<ChatRoomUser>, ChatRoom, ApplicationContext> = SQLTransactionSkript.transaction(ChatRoomSkripts.REMOVE_USER_PERMISSIONS)
    }

    abstract fun provider(): ApplicationContextProvider
    val userService = UserService(provider())

    abstract fun closeResources()

    override fun interceptSpec(context: Spec, spec: () -> Unit) {
        awaitSucceededFuture(provider().runOnContext(
                SQLTransactionSkript.transaction(ChatRoomSchema.dropAllAction),
                Unit))
        awaitSucceededFuture(provider().provideContext().flatMap { it.dropUserSchema() })
        awaitSucceededFuture(provider().provideContext().flatMap { it.initUserSchema() })
        awaitSucceededFuture(provider().runOnContext(
                SQLTransactionSkript.transaction(ChatRoomSchema.initAction),
                Unit))
        spec()
        awaitSucceededFuture(provider().runOnContext(
                SQLTransactionSkript.transaction<Unit, Unit, ApplicationContext>(ChatRoomSchema.dropAllAction),
                Unit))
        awaitSucceededFuture(provider().provideContext().flatMap { it.dropUserSchema() })
        closeResources()
    }

    init {
        "create a chatroom" {
            val user1 = UserFixture.generateUser(1)
            val user2 = UserFixture.generateUser(2)
            awaitSucceededFuture(userService.createUser(user1), user1.userProfile)
            awaitSucceededFuture(userService.createUser(user2), user2.userProfile)

            val session = awaitSucceededFuture(userService.loginUser(UserNameAndPassword(user1.userProfile.name, user1.password)))!!

            val chatRoomId = "chatId"
            val chatRoom = ChatRoom(
                    chatRoomId,
                    "name",
                    "A chat room is described",
                    setOf(
                            ChatRoomUser(
                                    Reference.Defined(user1.userProfile.id, user1.userProfile),
                                    Reference.Empty(chatRoomId),
                                    setOf(ChatRoomPermissionKey.AddUser.key,
                                            ChatRoomPermissionKey.RemoveUser.key,
                                            ChatRoomPermissionKey.AddPublicPermission.key,
                                            ChatRoomPermissionKey.RemovePublicPermission.key,
                                            ChatRoomPermissionKey.Update.key)),
                            ChatRoomUser(
                                    Reference.Defined(user2.userProfile.id, user2.userProfile),
                                    Reference.Empty(chatRoomId),
                                    setOf(ChatRoomPermissionKey.Update.key))),
                    setOf(ChatRoomPermissionKey.Get.key)
            )

            awaitSucceededFuture(provider().runOnContext(CREATE_CHAT_ROOM, TokenAndInput(session.sessionKey, chatRoom)), chatRoom)
            awaitSucceededFuture(provider().runOnContext(GET_CHAT_ROOM, TokenAndInput(session.sessionKey, chatRoomId)), chatRoom)

            val user3 = UserFixture.generateUser(3)
            awaitSucceededFuture(userService.createUser(user3), user3.userProfile)

            val chatRoomWithNewUser = chatRoom.copy(users = chatRoom.users.plus(ChatRoomUser(Reference.Defined(user3.userProfile.id, user3.userProfile), Reference.Empty(chatRoomId), setOf(
                    ChatRoomPermissionKey.Get.key,
                    ChatRoomPermissionKey.Update.key,
                    ChatRoomPermissionKey.AddUserPermission.key,
                    ChatRoomPermissionKey.RemoveUserPermission.key))))
            awaitSucceededFuture(
                    provider().runOnContext(
                            ADD_USER,
                            TokenAndInput(
                                    session.sessionKey,
                                    ChatRoomUser(
                                            Reference.Defined(user3.userProfile.id, user3.userProfile),
                                            Reference.Empty(chatRoomId),
                                            setOf(
                                                    ChatRoomPermissionKey.Get.key,
                                                    ChatRoomPermissionKey.Update.key,
                                                    ChatRoomPermissionKey.AddUserPermission.key,
                                                    ChatRoomPermissionKey.RemoveUserPermission.key)))),
                    chatRoomWithNewUser)
            val chatRoomAfterDeleteUser = chatRoomWithNewUser.copy(users = chatRoomWithNewUser.users.filterNot { it.user.id == user2.userProfile.id }.toSet())
            awaitSucceededFuture(
                    provider().runOnContext(
                            DELETE_USER,
                            TokenAndInput(session.sessionKey, ChatRoomUser(Reference.Empty(user2.userProfile.id), Reference.Empty(chatRoomId), setOf(ChatRoomPermissionKey.Update.key)))),
                    chatRoomAfterDeleteUser)

            awaitSucceededFuture(
                    provider().runOnContext(
                            ADD_PUBLIC_PERMISSIONS,
                            TokenAndInput(session.sessionKey, ChatRoomPermissions(Reference.Empty(chatRoomId), setOf(ChatRoomPermissionKey.AddUser.key)))),
                    chatRoomAfterDeleteUser.copy(publicPermissions = chatRoomAfterDeleteUser.publicPermissions.plus(ChatRoomPermissionKey.AddUser.key)))

            val nonPublicChatroom = chatRoomAfterDeleteUser.copy(publicPermissions = emptySet())

            awaitSucceededFuture(
                    provider().runOnContext(
                            REMOVE_PUBLIC_PERMISSIONS,
                            TokenAndInput(session.sessionKey, ChatRoomPermissions(Reference.Empty(chatRoomId), setOf(ChatRoomPermissionKey.Get.key, ChatRoomPermissionKey.AddUser.key)))),
                    nonPublicChatroom)

            val updatedChatroom = nonPublicChatroom.copy(name = "upname", description = "chatscription")
            awaitSucceededFuture(
                    provider().runOnContext(
                            UPDATE_CHAT_ROOM,
                            TokenAndInput(session.sessionKey, updatedChatroom)),
                    updatedChatroom
            )

            val session2 = awaitSucceededFuture(userService.loginUser(UserNameAndPassword(user3.userProfile.name, user3.password)))!!

            val chatRoomWithUser1AddedPermissions = updatedChatroom.copy(
                    users = updatedChatroom.users.map {
                        if(it.user.id == user1.userProfile.id) it.copy(permissions = it.permissions.plus(
                                setOf(
                                        ChatRoomPermissionKey.AddUserPermission.key,
                                        ChatRoomPermissionKey.RemoveUserPermission.key)))
                        else it
                    }.toSet())

            awaitFailedFuture(
                    provider().runOnContext(
                            ADD_USER_PERMISSION,
                            TokenAndInput(
                                session.sessionKey,
                                ChatRoomUser(Reference.Empty(user1.userProfile.id), Reference.Empty(chatRoomId), setOf(
                                        ChatRoomPermissionKey.AddUserPermission.key,
                                        ChatRoomPermissionKey.RemoveUserPermission.key
                            )))),
                    SQLError.OnCommand(
                            SQLCommand.Query(authorizeChatroomSelectStatement(chatRoomId, session.userId, ChatRoomPermissionKey.AddUserPermission.key)),
                            UserError.AuthorizationFailed))

            awaitSucceededFuture(
                    provider().runOnContext(
                            ADD_USER_PERMISSION,
                            TokenAndInput(
                                    session2.sessionKey,
                                    ChatRoomUser(Reference.Empty(user1.userProfile.id), Reference.Empty(chatRoomId), setOf(
                                            ChatRoomPermissionKey.AddUserPermission.key,
                                            ChatRoomPermissionKey.RemoveUserPermission.key
                                    )))),
                    chatRoomWithUser1AddedPermissions)

            val chatRoomWithUser3RemovedPermissions =
                    chatRoomWithUser1AddedPermissions.copy(
                            users = chatRoomWithUser1AddedPermissions.users.map {
                                if(it.user.id == user3.userProfile.id) {
                                    it.copy(permissions = setOf(
                                            ChatRoomPermissionKey.Get.key,
                                            ChatRoomPermissionKey.Update.key))
                                }
                                else it
                            }.toSet())

            awaitSucceededFuture(
                    provider().runOnContext(
                            REMOVE_USER_PERMISSION,
                            TokenAndInput(
                                    session.sessionKey,
                                    ChatRoomUser(
                                            Reference.Empty(user3.userProfile.id),
                                            Reference.Empty(chatRoomId),
                                            setOf(
                                                    ChatRoomPermissionKey.AddUserPermission.key,
                                                    ChatRoomPermissionKey.RemoveUserPermission.key
                                            )))),
                    chatRoomWithUser3RemovedPermissions)
        }
    }
    fun <T> awaitSucceededFuture(future: AsyncResult<T>, result: T? = null, maxDuration: Long = 1000L): T? {
        val start = System.currentTimeMillis()
        while(!future.isComplete() && System.currentTimeMillis() - start < maxDuration) {
            Thread.sleep(100)
        }
        if(!future.isComplete()) fail("Timeout")
        if(future.isFailure()) LOG.error("Expected Success", future.error())
        future.isSuccess() shouldBe true
        result?.let { future.result() shouldBe it }
        return future.result()
    }

    fun <T> awaitFailedFuture(future: AsyncResult<T>, cause: Throwable? = null, maxDuration: Long = 1000L): Throwable? {
        val start = System.currentTimeMillis()
        while(!future.isComplete() && System.currentTimeMillis() - start < maxDuration) {
            Thread.sleep(100)
        }
        future.isFailure() shouldBe true
        cause?.let { future.error() shouldBe it}
        return future.error()
    }



}