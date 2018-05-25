package playwrigkt.skript.chatroom

import io.kotlintest.Description
import io.kotlintest.Spec
import io.kotlintest.fail
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.slf4j.LoggerFactory
import playwrigkt.skript.Async
import playwrigkt.skript.Skript
import playwrigkt.skript.application.*
import playwrigkt.skript.auth.TokenAndInput
import playwrigkt.skript.chatroom.models.ChatRoom
import playwrigkt.skript.chatroom.models.ChatRoomPermissionKey
import playwrigkt.skript.chatroom.models.ChatRoomPermissions
import playwrigkt.skript.chatroom.models.ChatRoomUser
import playwrigkt.skript.common.models.Reference
import playwrigkt.skript.ex.createFile
import playwrigkt.skript.ex.join
import playwrigkt.skript.ex.readFile
import playwrigkt.skript.ex.writeFile
import playwrigkt.skript.file.FileReference
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.sql.transaction.SqlTransactionSkript
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.stagemanager.SyncJacksonSerializeStageManager
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.troupe.SyncFileTroupe
import playwrigkt.skript.user.UserFixture
import playwrigkt.skript.user.UserService
import playwrigkt.skript.user.extensions.schema.dropUserSchema
import playwrigkt.skript.user.extensions.schema.initUserSchema
import playwrigkt.skript.user.models.UserNameAndPassword
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.floor

abstract class ChatroomTransactionsSpec : StringSpec() {
    companion object {
        val CREATE_CHAT_ROOM: Skript<TokenAndInput<ChatRoom>, ChatRoom, ApplicationTroupe> = playwrigkt.skript.chatroom.ChatRoomSkripts.CREATE_CHAT_ROOM_TRANSACTION
        val GET_CHAT_ROOM: Skript<TokenAndInput<String>, ChatRoom, ApplicationTroupe> = playwrigkt.skript.chatroom.ChatRoomSkripts.GET_CHAT_ROOM_TRANSACTION
        val ADD_PUBLIC_PERMISSIONS: Skript<TokenAndInput<ChatRoomPermissions>, ChatRoom, ApplicationTroupe> = playwrigkt.skript.chatroom.ChatRoomSkripts.ADD_PERMISSIONS
        val REMOVE_PUBLIC_PERMISSIONS: Skript<TokenAndInput<ChatRoomPermissions>, ChatRoom, ApplicationTroupe> = playwrigkt.skript.chatroom.ChatRoomSkripts.REMOVE_PERMISSIONS
        val UPDATE_CHAT_ROOM: Skript<TokenAndInput<ChatRoom>, ChatRoom, ApplicationTroupe> = playwrigkt.skript.chatroom.ChatRoomSkripts.UPDATE_CHAT_ROOM_TRANSACTION
        val ADD_USER_PERMISSION: Skript<TokenAndInput<ChatRoomUser>, ChatRoom, ApplicationTroupe> = playwrigkt.skript.chatroom.ChatRoomSkripts.ADD_USER_PERMISSIONS
        val REMOVE_USER_PERMISSION: Skript<TokenAndInput<ChatRoomUser>, ChatRoom, ApplicationTroupe> = playwrigkt.skript.chatroom.ChatRoomSkripts.REMOVE_USER_PERMISSIONS
    }

    val log = LoggerFactory.getLogger(this.javaClass)

    abstract val sourceConfigFileName: String
    val port: Int = floor((Math.random() * 8000)).toInt() + 2000
    fun configFile() = sourceConfigFileName.split(".").joinToString("-$port.")

    val skriptApplication by lazy { Async.awaitSucceededFuture(createApplication(configFile()))!! }

    val stageManager by lazy  {
        skriptApplication.applicationResources
                .filter { it.value is ApplicationStageManager }
                .map { it.value as ApplicationStageManager }
                .first()
    }

    val userService by lazy { UserService(stageManager) }

    
    override fun beforeSpec(description: Description, spec: Spec) {
        val loader = SkriptApplicationLoader(SyncFileTroupe, SyncJacksonSerializeStageManager().hireTroupe(), ApplicationRegistry())
        Skript.identity<Unit, SkriptApplicationLoader>()
                .map { FileReference.Relative(sourceConfigFileName) }
                .readFile()
                .map { it.readText() }
                .map { it.replace("\"port\":8080", "\"port\":$port")}
                .split(Skript.identity<String, SkriptApplicationLoader>()
                        .map { FileReference.Relative(configFile()) }
                        .createFile()
                        .writeFile())
                .join { json, writer ->
                    writer.write(json)
                    writer.flush()
                    writer.close()
                }
                .run(Unit, loader)
        awaitSucceededFuture(stageManager.runWithTroupe(
                SqlTransactionSkript.transaction(playwrigkt.skript.chatroom.sql.ChatRoomSchema.dropAllAction),
                Unit))
        awaitSucceededFuture(stageManager.hireTroupe().dropUserSchema())
        awaitSucceededFuture(stageManager.hireTroupe().initUserSchema())
        awaitSucceededFuture(stageManager.runWithTroupe(
                SqlTransactionSkript.transaction(playwrigkt.skript.chatroom.sql.ChatRoomSchema.initAction),
                Unit))
    }

    override fun afterSpec(description: Description, spec: Spec) {
        awaitSucceededFuture(stageManager.runWithTroupe(
                SqlTransactionSkript.transaction(playwrigkt.skript.chatroom.sql.ChatRoomSchema.dropAllAction),
                Unit))
        awaitSucceededFuture(stageManager.hireTroupe().dropUserSchema())
        awaitSucceededFuture(skriptApplication.tearDown())
        Files.delete(Paths.get(configFile()))
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
                                    Reference.defined(user1.userProfile.id, user1.userProfile),
                                    Reference.empty(chatRoomId),
                                    setOf(ChatRoomPermissionKey.AddUserPermission.key,
                                            ChatRoomPermissionKey.RemoveUserPermission.key,
                                            ChatRoomPermissionKey.AddPublicPermission.key,
                                            ChatRoomPermissionKey.RemovePublicPermission.key,
                                            ChatRoomPermissionKey.Update.key)),
                            ChatRoomUser(
                                    Reference.defined(user2.userProfile.id, user2.userProfile),
                                    Reference.empty(chatRoomId),
                                    setOf(ChatRoomPermissionKey.Update.key))),
                    setOf(ChatRoomPermissionKey.Get.key)
            )

            awaitSucceededFuture(stageManager.runWithTroupe(CREATE_CHAT_ROOM, TokenAndInput(session.sessionKey, chatRoom)), chatRoom)
            awaitSucceededFuture(stageManager.runWithTroupe(GET_CHAT_ROOM, TokenAndInput(session.sessionKey, chatRoomId)), chatRoom)

            val user3 = UserFixture.generateUser(3)
            awaitSucceededFuture(userService.createUser(user3), user3.userProfile)

            val chatRoomWithNewUser = chatRoom.copy(users = chatRoom.users.plus(ChatRoomUser(Reference.defined(user3.userProfile.id, user3.userProfile), Reference.empty(chatRoomId), setOf(
                    ChatRoomPermissionKey.Get.key,
                    ChatRoomPermissionKey.Update.key,
                    ChatRoomPermissionKey.AddUserPermission.key,
                    ChatRoomPermissionKey.RemoveUserPermission.key))))
            awaitSucceededFuture(
                    stageManager.runWithTroupe(
                            ADD_USER_PERMISSION,
                            TokenAndInput(
                                    session.sessionKey,
                                    ChatRoomUser(
                                            Reference.defined(user3.userProfile.id, user3.userProfile),
                                            Reference.empty(chatRoomId),
                                            setOf(
                                                    ChatRoomPermissionKey.Get.key,
                                                    ChatRoomPermissionKey.Update.key,
                                                    ChatRoomPermissionKey.AddUserPermission.key,
                                                    ChatRoomPermissionKey.RemoveUserPermission.key)))),
                    chatRoomWithNewUser)
            val chatRoomAfterDeleteUser = chatRoomWithNewUser.copy(users = chatRoomWithNewUser.users.filterNot { it.user.id == user2.userProfile.id }.toSet())
            awaitSucceededFuture(
                    stageManager.runWithTroupe(
                            REMOVE_USER_PERMISSION,
                            TokenAndInput(session.sessionKey, ChatRoomUser(Reference.empty(user2.userProfile.id), Reference.empty(chatRoomId), setOf(ChatRoomPermissionKey.Update.key)))),
                    chatRoomAfterDeleteUser)

            awaitSucceededFuture(
                    stageManager.runWithTroupe(
                            ADD_PUBLIC_PERMISSIONS,
                            TokenAndInput(session.sessionKey, ChatRoomPermissions(Reference.empty(chatRoomId), setOf(ChatRoomPermissionKey.AddUserPermission.key)))),
                    chatRoomAfterDeleteUser.copy(publicPermissions = chatRoomAfterDeleteUser.publicPermissions.plus(ChatRoomPermissionKey.AddUserPermission.key)))

            val nonPublicChatroom = chatRoomAfterDeleteUser.copy(publicPermissions = emptySet())

            awaitSucceededFuture(
                    stageManager.runWithTroupe(
                            REMOVE_PUBLIC_PERMISSIONS,
                            TokenAndInput(session.sessionKey, ChatRoomPermissions(Reference.empty(chatRoomId), setOf(ChatRoomPermissionKey.Get.key, ChatRoomPermissionKey.AddUserPermission.key)))),
                    nonPublicChatroom)

            val updatedChatroom = nonPublicChatroom.copy(name = "upname", description = "chatscription")
            awaitSucceededFuture(
                    stageManager.runWithTroupe(
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
                    stageManager.runWithTroupe(
                            REMOVE_USER_PERMISSION,
                            TokenAndInput(
                                    session.sessionKey,
                                    ChatRoomUser(
                                            Reference.empty(user3.userProfile.id),
                                            Reference.empty(chatRoomId),
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
        if(future.isFailure()) log.error("Expected Success", future.error())
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