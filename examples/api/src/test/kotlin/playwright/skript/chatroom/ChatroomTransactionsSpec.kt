package playwright.skript.chatroom

import io.kotlintest.Spec
import io.kotlintest.matchers.fail
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import org.slf4j.LoggerFactory
import playwright.skript.Skript
import playwright.skript.chatrooom.sql.query.authorizeChatroomSelectStatement
import playwright.skript.common.ApplicationStage
import playwright.skript.common.ApplicationVenue
import playwright.skript.common.models.Reference
import playwright.skript.result.AsyncResult
import playwright.skript.sql.SQLCommand
import playwright.skript.sql.SQLError
import playwright.skript.sql.transaction.SQLTransactionSkript
import playwright.skript.user.UserFixture
import playwright.skript.user.UserService
import playwright.skript.user.extensions.schema.dropUserSchema
import playwright.skript.user.extensions.schema.initUserSchema
import playwright.skript.user.models.UserError
import playwright.skript.user.models.UserNameAndPassword

abstract class ChatroomTransactionsSpec : StringSpec() {

    val LOG = LoggerFactory.getLogger(this.javaClass)

    companion object {
        val CREATE_CHAT_ROOM: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoom>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> = SQLTransactionSkript.transaction(playwright.skript.chatrooom.ChatRoomSkripts.CREATE_CHAT_ROOM_TRANSACTION)
        val GET_CHAT_ROOM: Skript<playwright.skript.auth.TokenAndInput<String>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> = SQLTransactionSkript.transaction(playwright.skript.chatrooom.ChatRoomSkripts.GET_CHAT_ROOM_TRANSACTION)
        val ADD_USER: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> = SQLTransactionSkript.transaction(playwright.skript.chatrooom.ChatRoomSkripts.ADD_USER_TRANSACTION)
        val DELETE_USER: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> = SQLTransactionSkript.transaction(playwright.skript.chatrooom.ChatRoomSkripts.REMOVE_USER_TRANSACTION)
        val ADD_PUBLIC_PERMISSIONS: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoomPermissions>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> = SQLTransactionSkript.transaction(playwright.skript.chatrooom.ChatRoomSkripts.ADD_PERMISSIONS)
        val REMOVE_PUBLIC_PERMISSIONS: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoomPermissions>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> = SQLTransactionSkript.transaction(playwright.skript.chatrooom.ChatRoomSkripts.REMOVE_PERMISSIONS)
        val UPDATE_CHAT_ROOM: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoom>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> = SQLTransactionSkript.transaction(playwright.skript.chatrooom.ChatRoomSkripts.UPDATE_CHAT_ROOM_TRANSACTION)
        val ADD_USER_PERMISSION: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> = SQLTransactionSkript.transaction(playwright.skript.chatrooom.ChatRoomSkripts.ADD_USER_PERMISSIONS)
        val REMOVE_USER_PERMISSION: Skript<playwright.skript.auth.TokenAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, playwright.skript.chatrooom.models.ChatRoom, ApplicationStage> = SQLTransactionSkript.transaction(playwright.skript.chatrooom.ChatRoomSkripts.REMOVE_USER_PERMISSIONS)
    }

    abstract fun provider(): ApplicationVenue
    val userService = UserService(provider())

    abstract fun closeResources()

    override fun interceptSpec(context: Spec, spec: () -> Unit) {
        awaitSucceededFuture(provider().runOnStage(
                SQLTransactionSkript.transaction(playwright.skript.chatrooom.sql.ChatRoomSchema.dropAllAction),
                Unit))
        awaitSucceededFuture(provider().provideStage().flatMap { it.dropUserSchema() })
        awaitSucceededFuture(provider().provideStage().flatMap { it.initUserSchema() })
        awaitSucceededFuture(provider().runOnStage(
                SQLTransactionSkript.transaction(playwright.skript.chatrooom.sql.ChatRoomSchema.initAction),
                Unit))
        spec()
        awaitSucceededFuture(provider().runOnStage(
                SQLTransactionSkript.transaction<Unit, Unit, ApplicationStage>(playwright.skript.chatrooom.sql.ChatRoomSchema.dropAllAction),
                Unit))
        awaitSucceededFuture(provider().provideStage().flatMap { it.dropUserSchema() })
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
            val chatRoom = playwright.skript.chatrooom.models.ChatRoom(
                    chatRoomId,
                    "name",
                    "A chat room is described",
                    setOf(
                            playwright.skript.chatrooom.models.ChatRoomUser(
                                    Reference.Defined(user1.userProfile.id, user1.userProfile),
                                    Reference.Empty(chatRoomId),
                                    setOf(playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddUser.key,
                                            playwright.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUser.key,
                                            playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddPublicPermission.key,
                                            playwright.skript.chatrooom.models.ChatRoomPermissionKey.RemovePublicPermission.key,
                                            playwright.skript.chatrooom.models.ChatRoomPermissionKey.Update.key)),
                            playwright.skript.chatrooom.models.ChatRoomUser(
                                    Reference.Defined(user2.userProfile.id, user2.userProfile),
                                    Reference.Empty(chatRoomId),
                                    setOf(playwright.skript.chatrooom.models.ChatRoomPermissionKey.Update.key))),
                    setOf(playwright.skript.chatrooom.models.ChatRoomPermissionKey.Get.key)
            )

            awaitSucceededFuture(provider().runOnStage(CREATE_CHAT_ROOM, playwright.skript.auth.TokenAndInput(session.sessionKey, chatRoom)), chatRoom)
            awaitSucceededFuture(provider().runOnStage(GET_CHAT_ROOM, playwright.skript.auth.TokenAndInput(session.sessionKey, chatRoomId)), chatRoom)

            val user3 = UserFixture.generateUser(3)
            awaitSucceededFuture(userService.createUser(user3), user3.userProfile)

            val chatRoomWithNewUser = chatRoom.copy(users = chatRoom.users.plus(playwright.skript.chatrooom.models.ChatRoomUser(Reference.Defined(user3.userProfile.id, user3.userProfile), Reference.Empty(chatRoomId), setOf(
                    playwright.skript.chatrooom.models.ChatRoomPermissionKey.Get.key,
                    playwright.skript.chatrooom.models.ChatRoomPermissionKey.Update.key,
                    playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key,
                    playwright.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key))))
            awaitSucceededFuture(
                    provider().runOnStage(
                            ADD_USER,
                            playwright.skript.auth.TokenAndInput(
                                    session.sessionKey,
                                    playwright.skript.chatrooom.models.ChatRoomUser(
                                            Reference.Defined(user3.userProfile.id, user3.userProfile),
                                            Reference.Empty(chatRoomId),
                                            setOf(
                                                    playwright.skript.chatrooom.models.ChatRoomPermissionKey.Get.key,
                                                    playwright.skript.chatrooom.models.ChatRoomPermissionKey.Update.key,
                                                    playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key,
                                                    playwright.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key)))),
                    chatRoomWithNewUser)
            val chatRoomAfterDeleteUser = chatRoomWithNewUser.copy(users = chatRoomWithNewUser.users.filterNot { it.user.id == user2.userProfile.id }.toSet())
            awaitSucceededFuture(
                    provider().runOnStage(
                            DELETE_USER,
                            playwright.skript.auth.TokenAndInput(session.sessionKey, playwright.skript.chatrooom.models.ChatRoomUser(Reference.Empty(user2.userProfile.id), Reference.Empty(chatRoomId), setOf(playwright.skript.chatrooom.models.ChatRoomPermissionKey.Update.key)))),
                    chatRoomAfterDeleteUser)

            awaitSucceededFuture(
                    provider().runOnStage(
                            ADD_PUBLIC_PERMISSIONS,
                            playwright.skript.auth.TokenAndInput(session.sessionKey, playwright.skript.chatrooom.models.ChatRoomPermissions(Reference.Empty(chatRoomId), setOf(playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddUser.key)))),
                    chatRoomAfterDeleteUser.copy(publicPermissions = chatRoomAfterDeleteUser.publicPermissions.plus(playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddUser.key)))

            val nonPublicChatroom = chatRoomAfterDeleteUser.copy(publicPermissions = emptySet())

            awaitSucceededFuture(
                    provider().runOnStage(
                            REMOVE_PUBLIC_PERMISSIONS,
                            playwright.skript.auth.TokenAndInput(session.sessionKey, playwright.skript.chatrooom.models.ChatRoomPermissions(Reference.Empty(chatRoomId), setOf(playwright.skript.chatrooom.models.ChatRoomPermissionKey.Get.key, playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddUser.key)))),
                    nonPublicChatroom)

            val updatedChatroom = nonPublicChatroom.copy(name = "upname", description = "chatscription")
            awaitSucceededFuture(
                    provider().runOnStage(
                            UPDATE_CHAT_ROOM,
                            playwright.skript.auth.TokenAndInput(session.sessionKey, updatedChatroom)),
                    updatedChatroom
            )

            val session2 = awaitSucceededFuture(userService.loginUser(UserNameAndPassword(user3.userProfile.name, user3.password)))!!

            val chatRoomWithUser1AddedPermissions = updatedChatroom.copy(
                    users = updatedChatroom.users.map {
                        if(it.user.id == user1.userProfile.id) it.copy(permissions = it.permissions.plus(
                                setOf(
                                        playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key,
                                        playwright.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key)))
                        else it
                    }.toSet())

            awaitFailedFuture(
                    provider().runOnStage(
                            ADD_USER_PERMISSION,
                            playwright.skript.auth.TokenAndInput(
                                    session.sessionKey,
                                    playwright.skript.chatrooom.models.ChatRoomUser(Reference.Empty(user1.userProfile.id), Reference.Empty(chatRoomId), setOf(
                                            playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key,
                                            playwright.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key
                                    )))),
                    SQLError.OnCommand(
                            SQLCommand.Query(authorizeChatroomSelectStatement(chatRoomId, session.userId, playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key)),
                            UserError.AuthorizationFailed))

            awaitSucceededFuture(
                    provider().runOnStage(
                            ADD_USER_PERMISSION,
                            playwright.skript.auth.TokenAndInput(
                                    session2.sessionKey,
                                    playwright.skript.chatrooom.models.ChatRoomUser(Reference.Empty(user1.userProfile.id), Reference.Empty(chatRoomId), setOf(
                                            playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key,
                                            playwright.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key
                                    )))),
                    chatRoomWithUser1AddedPermissions)

            val chatRoomWithUser3RemovedPermissions =
                    chatRoomWithUser1AddedPermissions.copy(
                            users = chatRoomWithUser1AddedPermissions.users.map {
                                if(it.user.id == user3.userProfile.id) {
                                    it.copy(permissions = setOf(
                                            playwright.skript.chatrooom.models.ChatRoomPermissionKey.Get.key,
                                            playwright.skript.chatrooom.models.ChatRoomPermissionKey.Update.key))
                                }
                                else it
                            }.toSet())

            awaitSucceededFuture(
                    provider().runOnStage(
                            REMOVE_USER_PERMISSION,
                            playwright.skript.auth.TokenAndInput(
                                    session.sessionKey,
                                    playwright.skript.chatrooom.models.ChatRoomUser(
                                            Reference.Empty(user3.userProfile.id),
                                            Reference.Empty(chatRoomId),
                                            setOf(
                                                    playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key,
                                                    playwright.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key
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