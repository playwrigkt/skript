package playwrigkt.skript.chatroom

import io.kotlintest.Description
import io.kotlintest.Spec
import io.kotlintest.fail
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.slf4j.LoggerFactory
import playwrigkt.skript.Skript
import playwrigkt.skript.common.models.Reference
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.sql.transaction.SQLTransactionSkript
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.UserFixture
import playwrigkt.skript.user.UserService
import playwrigkt.skript.user.extensions.schema.dropUserSchema
import playwrigkt.skript.user.extensions.schema.initUserSchema
import playwrigkt.skript.user.models.UserError
import playwrigkt.skript.user.models.UserNameAndPassword

abstract class ChatroomTransactionsSpec : StringSpec() {

    val LOG = LoggerFactory.getLogger(this.javaClass)

    companion object {
        val CREATE_CHAT_ROOM: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> = SQLTransactionSkript.transaction(playwrigkt.skript.chatrooom.ChatRoomSkripts.CREATE_CHAT_ROOM_TRANSACTION)
        val GET_CHAT_ROOM: Skript<playwrigkt.skript.auth.TokenAndInput<String>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> = SQLTransactionSkript.transaction(playwrigkt.skript.chatrooom.ChatRoomSkripts.GET_CHAT_ROOM_TRANSACTION)
        val ADD_USER: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> = SQLTransactionSkript.transaction(playwrigkt.skript.chatrooom.ChatRoomSkripts.ADD_USER_TRANSACTION)
        val DELETE_USER: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> = SQLTransactionSkript.transaction(playwrigkt.skript.chatrooom.ChatRoomSkripts.REMOVE_USER_TRANSACTION)
        val ADD_PUBLIC_PERMISSIONS: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> = SQLTransactionSkript.transaction(playwrigkt.skript.chatrooom.ChatRoomSkripts.ADD_PERMISSIONS)
        val REMOVE_PUBLIC_PERMISSIONS: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> = SQLTransactionSkript.transaction(playwrigkt.skript.chatrooom.ChatRoomSkripts.REMOVE_PERMISSIONS)
        val UPDATE_CHAT_ROOM: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> = SQLTransactionSkript.transaction(playwrigkt.skript.chatrooom.ChatRoomSkripts.UPDATE_CHAT_ROOM_TRANSACTION)
        val ADD_USER_PERMISSION: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> = SQLTransactionSkript.transaction(playwrigkt.skript.chatrooom.ChatRoomSkripts.ADD_USER_PERMISSIONS)
        val REMOVE_USER_PERMISSION: Skript<playwrigkt.skript.auth.TokenAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, playwrigkt.skript.chatrooom.models.ChatRoom, ApplicationTroupe> = SQLTransactionSkript.transaction(playwrigkt.skript.chatrooom.ChatRoomSkripts.REMOVE_USER_PERMISSIONS)
    }

    abstract fun stageManager(): ApplicationStageManager
    val userService by lazy { UserService(stageManager()) }

    override fun beforeSpec(description: Description, spec: Spec) {
        awaitSucceededFuture(stageManager().runWithTroupe(
                SQLTransactionSkript.transaction(playwrigkt.skript.chatrooom.sql.ChatRoomSchema.dropAllAction),
                Unit))
        awaitSucceededFuture(stageManager().hireTroupe().dropUserSchema())
        awaitSucceededFuture(stageManager().hireTroupe().initUserSchema())
        awaitSucceededFuture(stageManager().runWithTroupe(
                SQLTransactionSkript.transaction(playwrigkt.skript.chatrooom.sql.ChatRoomSchema.initAction),
                Unit))
    }

    override fun afterSpec(description: Description, spec: Spec) {
        awaitSucceededFuture(stageManager().runWithTroupe(
                SQLTransactionSkript.transaction<Unit, Unit, ApplicationTroupe>(playwrigkt.skript.chatrooom.sql.ChatRoomSchema.dropAllAction),
                Unit))
        awaitSucceededFuture(stageManager().hireTroupe().dropUserSchema())
        awaitSucceededFuture(stageManager().tearDown())
    }

    init {
        "create a chatroom" {
            val user1 = UserFixture.generateUser(1)
            val user2 = UserFixture.generateUser(2)
            awaitSucceededFuture(userService.createUser(user1), user1.userProfile)
            awaitSucceededFuture(userService.createUser(user2), user2.userProfile)

            val session = awaitSucceededFuture(userService.loginUser(UserNameAndPassword(user1.userProfile.name, user1.password)))!!

            val chatRoomId = "chatId"
            val chatRoom = playwrigkt.skript.chatrooom.models.ChatRoom(
                    chatRoomId,
                    "name",
                    "A chat room is described",
                    setOf(
                            playwrigkt.skript.chatrooom.models.ChatRoomUser(
                                    Reference.Defined(user1.userProfile.id, user1.userProfile),
                                    Reference.Empty(chatRoomId),
                                    setOf(playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddUser.key,
                                            playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUser.key,
                                            playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddPublicPermission.key,
                                            playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.RemovePublicPermission.key,
                                            playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Update.key)),
                            playwrigkt.skript.chatrooom.models.ChatRoomUser(
                                    Reference.Defined(user2.userProfile.id, user2.userProfile),
                                    Reference.Empty(chatRoomId),
                                    setOf(playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Update.key))),
                    setOf(playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Get.key)
            )

            awaitSucceededFuture(stageManager().runWithTroupe(CREATE_CHAT_ROOM, playwrigkt.skript.auth.TokenAndInput(session.sessionKey, chatRoom)), chatRoom)
            awaitSucceededFuture(stageManager().runWithTroupe(GET_CHAT_ROOM, playwrigkt.skript.auth.TokenAndInput(session.sessionKey, chatRoomId)), chatRoom)

            val user3 = UserFixture.generateUser(3)
            awaitSucceededFuture(userService.createUser(user3), user3.userProfile)

            val chatRoomWithNewUser = chatRoom.copy(users = chatRoom.users.plus(playwrigkt.skript.chatrooom.models.ChatRoomUser(Reference.Defined(user3.userProfile.id, user3.userProfile), Reference.Empty(chatRoomId), setOf(
                    playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Get.key,
                    playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Update.key,
                    playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key,
                    playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key))))
            awaitSucceededFuture(
                    stageManager().runWithTroupe(
                            ADD_USER,
                            playwrigkt.skript.auth.TokenAndInput(
                                    session.sessionKey,
                                    playwrigkt.skript.chatrooom.models.ChatRoomUser(
                                            Reference.Defined(user3.userProfile.id, user3.userProfile),
                                            Reference.Empty(chatRoomId),
                                            setOf(
                                                    playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Get.key,
                                                    playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Update.key,
                                                    playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key,
                                                    playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key)))),
                    chatRoomWithNewUser)
            val chatRoomAfterDeleteUser = chatRoomWithNewUser.copy(users = chatRoomWithNewUser.users.filterNot { it.user.id == user2.userProfile.id }.toSet())
            awaitSucceededFuture(
                    stageManager().runWithTroupe(
                            DELETE_USER,
                            playwrigkt.skript.auth.TokenAndInput(session.sessionKey, playwrigkt.skript.chatrooom.models.ChatRoomUser(Reference.Empty(user2.userProfile.id), Reference.Empty(chatRoomId), setOf(playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Update.key)))),
                    chatRoomAfterDeleteUser)

            awaitSucceededFuture(
                    stageManager().runWithTroupe(
                            ADD_PUBLIC_PERMISSIONS,
                            playwrigkt.skript.auth.TokenAndInput(session.sessionKey, playwrigkt.skript.chatrooom.models.ChatRoomPermissions(Reference.Empty(chatRoomId), setOf(playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddUser.key)))),
                    chatRoomAfterDeleteUser.copy(publicPermissions = chatRoomAfterDeleteUser.publicPermissions.plus(playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddUser.key)))

            val nonPublicChatroom = chatRoomAfterDeleteUser.copy(publicPermissions = emptySet())

            awaitSucceededFuture(
                    stageManager().runWithTroupe(
                            REMOVE_PUBLIC_PERMISSIONS,
                            playwrigkt.skript.auth.TokenAndInput(session.sessionKey, playwrigkt.skript.chatrooom.models.ChatRoomPermissions(Reference.Empty(chatRoomId), setOf(playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Get.key, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddUser.key)))),
                    nonPublicChatroom)

            val updatedChatroom = nonPublicChatroom.copy(name = "upname", description = "chatscription")
            awaitSucceededFuture(
                    stageManager().runWithTroupe(
                            UPDATE_CHAT_ROOM,
                            playwrigkt.skript.auth.TokenAndInput(session.sessionKey, updatedChatroom)),
                    updatedChatroom
            )

            val session2 = awaitSucceededFuture(userService.loginUser(UserNameAndPassword(user3.userProfile.name, user3.password)))!!

            val chatRoomWithUser1AddedPermissions = updatedChatroom.copy(
                    users = updatedChatroom.users.map {
                        if(it.user.id == user1.userProfile.id) it.copy(permissions = it.permissions.plus(
                                setOf(
                                        playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key,
                                        playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key)))
                        else it
                    }.toSet())

            awaitFailedFuture(
                    stageManager().runWithTroupe(
                            ADD_USER_PERMISSION,
                            playwrigkt.skript.auth.TokenAndInput(
                                    session.sessionKey,
                                    playwrigkt.skript.chatrooom.models.ChatRoomUser(Reference.Empty(user1.userProfile.id), Reference.Empty(chatRoomId), setOf(
                                            playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key,
                                            playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key
                                    )))),
                            UserError.AuthorizationFailed)

            awaitSucceededFuture(
                    stageManager().runWithTroupe(
                            ADD_USER_PERMISSION,
                            playwrigkt.skript.auth.TokenAndInput(
                                    session2.sessionKey,
                                    playwrigkt.skript.chatrooom.models.ChatRoomUser(Reference.Empty(user1.userProfile.id), Reference.Empty(chatRoomId), setOf(
                                            playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key,
                                            playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key
                                    )))),
                    chatRoomWithUser1AddedPermissions)

            val chatRoomWithUser3RemovedPermissions =
                    chatRoomWithUser1AddedPermissions.copy(
                            users = chatRoomWithUser1AddedPermissions.users.map {
                                if(it.user.id == user3.userProfile.id) {
                                    it.copy(permissions = setOf(
                                            playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Get.key,
                                            playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Update.key))
                                }
                                else it
                            }.toSet())

            awaitSucceededFuture(
                    stageManager().runWithTroupe(
                            REMOVE_USER_PERMISSION,
                            playwrigkt.skript.auth.TokenAndInput(
                                    session.sessionKey,
                                    playwrigkt.skript.chatrooom.models.ChatRoomUser(
                                            Reference.Empty(user3.userProfile.id),
                                            Reference.Empty(chatRoomId),
                                            setOf(
                                                    playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key,
                                                    playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key
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