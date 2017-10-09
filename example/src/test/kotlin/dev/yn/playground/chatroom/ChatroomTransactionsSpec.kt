package dev.yn.playground.chatroom

import dev.yn.playground.auth.TokenAndInput
import dev.yn.playground.chatrooom.models.ChatRoom
import dev.yn.playground.chatrooom.models.ChatRoomPermissionKey
import dev.yn.playground.chatrooom.models.ChatRoomPermissions
import dev.yn.playground.chatrooom.models.ChatRoomUser
import dev.yn.playground.chatrooom.sql.ChatRoomSchema
import dev.yn.playground.chatrooom.sql.ChatRoomTransactions
import dev.yn.playground.chatrooom.sql.query.authorizeChatroomSelectStatement
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.common.models.Reference
import dev.yn.playground.sql.SQLError
import dev.yn.playground.sql.task.UnpreparedTransactionalSQLTask
import dev.yn.playground.task.Task
import dev.yn.playground.user.UserError
import dev.yn.playground.user.UserFixture
import dev.yn.playground.user.UserNameAndPassword
import dev.yn.playground.user.extensions.schema.dropUserSchema
import dev.yn.playground.user.extensions.schema.initUserSchema
import io.kotlintest.Spec
import io.kotlintest.TestCaseContext
import io.kotlintest.matchers.fail
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import org.slf4j.LoggerFactory
import java.sql.Ref

class ChatroomTransactionsSpec : StringSpec() {

    val LOG = LoggerFactory.getLogger(this.javaClass)



    companion object {
        val hikariConfig = JsonObject()
                .put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider")
                .put("jdbcUrl", "jdbc:postgresql://localhost:5432/chitchat")
                .put("username", "chatty_tammy")
                .put("password", "gossipy")
                .put("driver_class", "org.postgresql.Driver")
                .put("maximumPoolSize", 30)
                .put("poolName", "test_pool")

        val vertx by lazy { Vertx.vertx() }

        val sqlClient: SQLClient by lazy {
            JDBCClient.createShared(vertx, hikariConfig, "test_ds")
        }

        val provider: ApplicationContextProvider by lazy {
            ApplicationContextProvider(vertx, sqlClient)
        }

        val userService by lazy {
            dev.yn.playground.user.UserService(sqlClient, vertx)
        }

        val createChatRoom: Task<TokenAndInput<ChatRoom>, ChatRoom> by lazy { UnpreparedTransactionalSQLTask(ChatRoomTransactions.createChatRoomTransaction).prepare(provider) }
        val getChatRoom: Task<TokenAndInput<String>, ChatRoom> by lazy { UnpreparedTransactionalSQLTask(ChatRoomTransactions.getChatRoomTransaction).prepare(provider) }
        val addUser: Task<TokenAndInput<ChatRoomUser>, ChatRoom> by lazy { UnpreparedTransactionalSQLTask(ChatRoomTransactions.addUserTransaction).prepare(provider) }
        val deleteUser: Task<TokenAndInput<ChatRoomUser>, ChatRoom> by lazy { UnpreparedTransactionalSQLTask(ChatRoomTransactions.removeUserTransaction).prepare(provider) }
        val addPublicPermissions: Task<TokenAndInput<ChatRoomPermissions>, ChatRoom> by lazy { UnpreparedTransactionalSQLTask(ChatRoomTransactions.addPermissions).prepare(provider) }
        val removePublicPermissions: Task<TokenAndInput<ChatRoomPermissions>, ChatRoom> by lazy { UnpreparedTransactionalSQLTask(ChatRoomTransactions.removePermissions).prepare(provider) }
        val updateChatRoom: Task<TokenAndInput<ChatRoom>, ChatRoom> by lazy { UnpreparedTransactionalSQLTask(ChatRoomTransactions.updateChatRoomTransaction).prepare(provider) }
        val addUserPermission by lazy { UnpreparedTransactionalSQLTask(ChatRoomTransactions.addUserPermissions).prepare(provider) }
        val removeUserPermission by lazy { UnpreparedTransactionalSQLTask(ChatRoomTransactions.removeUserPermissions).prepare(provider) }
    }



    override fun interceptTestCase(context: TestCaseContext, test: () -> Unit) {
        test()
    }

    override fun interceptSpec(context: Spec, spec: () -> Unit) {
        awaitSucceededFuture(UnpreparedTransactionalSQLTask(ChatRoomSchema.dropAllAction).prepare(provider).run(Unit))
        awaitSucceededFuture(provider.dropUserSchema())
        awaitSucceededFuture(provider.initUserSchema())
        awaitSucceededFuture(UnpreparedTransactionalSQLTask(ChatRoomSchema.initAction).prepare(provider).run(Unit))
        spec()
        awaitSucceededFuture(UnpreparedTransactionalSQLTask(ChatRoomSchema.dropAllAction).prepare(provider).run(Unit))
        awaitSucceededFuture(provider.dropUserSchema())
        val clientF = Future.future<Void>()
        sqlClient.close(clientF.completer())
        awaitSucceededFuture(clientF)
        val future = Future.future<Void>()
        vertx.close(future.completer())
        awaitSucceededFuture(future)
    }

    init {
        "create a chatroom" {
            val user1 = UserFixture.generateUser(1)
            val user2 = UserFixture.generateUser(2)
            awaitSucceededFuture(userService.createUser(user1), user1.userProfile)
            awaitSucceededFuture(userService.createUser(user2), user2.userProfile)

            val session = awaitSucceededFuture(userService.loginUser(UserNameAndPassword(user1.userProfile.name, user1.password)))

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

            awaitSucceededFuture(createChatRoom.run(TokenAndInput(session.sessionKey, chatRoom)), chatRoom)
            awaitSucceededFuture(getChatRoom.run(TokenAndInput(session.sessionKey, chatRoomId)), chatRoom)

            val user3 = UserFixture.generateUser(3)
            awaitSucceededFuture(userService.createUser(user3), user3.userProfile)

            val chatRoomWithNewUser = chatRoom.copy(users = chatRoom.users.plus(ChatRoomUser(Reference.Defined(user3.userProfile.id, user3.userProfile), Reference.Empty(chatRoomId), setOf(
                    ChatRoomPermissionKey.Get.key,
                    ChatRoomPermissionKey.Update.key,
                    ChatRoomPermissionKey.AddUserPermission.key,
                    ChatRoomPermissionKey.RemoveUserPermission.key))))
            awaitSucceededFuture(
                    addUser.run(TokenAndInput(
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
                    deleteUser.run(TokenAndInput(session.sessionKey, ChatRoomUser(Reference.Empty(user2.userProfile.id), Reference.Empty(chatRoomId), setOf(ChatRoomPermissionKey.Update.key)))),
                    chatRoomAfterDeleteUser)

            awaitSucceededFuture(
                    addPublicPermissions.run(TokenAndInput(session.sessionKey, ChatRoomPermissions(Reference.Empty(chatRoomId), setOf(ChatRoomPermissionKey.AddUser.key)))),
                    chatRoomAfterDeleteUser.copy(publicPermissions = chatRoomAfterDeleteUser.publicPermissions.plus(ChatRoomPermissionKey.AddUser.key)))

            val nonPublicChatroom = chatRoomAfterDeleteUser.copy(publicPermissions = emptySet())

            awaitSucceededFuture(
                    removePublicPermissions.run(TokenAndInput(session.sessionKey, ChatRoomPermissions(Reference.Empty(chatRoomId), setOf(ChatRoomPermissionKey.Get.key, ChatRoomPermissionKey.AddUser.key)))),
                    nonPublicChatroom)

            val updatedChatroom = nonPublicChatroom.copy(name = "upname", description = "chatscription")
            awaitSucceededFuture(
                    updateChatRoom.run(TokenAndInput(session.sessionKey, updatedChatroom)),
                    updatedChatroom
            )

            val session2 = awaitSucceededFuture(userService.loginUser(UserNameAndPassword(user3.userProfile.name, user3.password)))

            val chatRoomWithUser1AddedPermissions = updatedChatroom.copy(
                    users = updatedChatroom.users.map {
                        if(it.user.id == user1.userProfile.id) it.copy(permissions = it.permissions.plus(
                                setOf(
                                        ChatRoomPermissionKey.AddUserPermission.key,
                                        ChatRoomPermissionKey.RemoveUserPermission.key)))
                        else it
                    }.toSet())

            awaitFailedFuture(
                    addUserPermission.run(TokenAndInput(
                            session.sessionKey,
                            ChatRoomUser(Reference.Empty(user1.userProfile.id), Reference.Empty(chatRoomId), setOf(
                                    ChatRoomPermissionKey.AddUserPermission.key,
                                    ChatRoomPermissionKey.RemoveUserPermission.key
                            )))),
                    SQLError.OnStatement(authorizeChatroomSelectStatement(chatRoomId, session.userId, ChatRoomPermissionKey.AddUserPermission.key),  UserError.AuthorizationFailed))

            awaitSucceededFuture(
                    addUserPermission.run(TokenAndInput(
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
                    removeUserPermission.run(TokenAndInput(
                            session.sessionKey,
                            ChatRoomUser(
                                    Reference.Empty(user3.userProfile.id),
                                    Reference.Empty(chatRoomId),
                                    setOf(
                                            ChatRoomPermissionKey.AddUserPermission.key,
                                            ChatRoomPermissionKey.RemoveUserPermission.key
                                    ))
                    )),
                    chatRoomWithUser3RemovedPermissions)
        }
    }

    fun <T> awaitSucceededFuture(future: Future<T>, result: T? = null, maxDuration: Long = 1000L): T {
        val start = System.currentTimeMillis()
        while(!future.isComplete && System.currentTimeMillis() - start < maxDuration) {
            Thread.sleep(100)
        }
        if(!future.isComplete) fail("Timeout")
        if(future.failed()) LOG.error("Expected Success", future.cause())
        future.succeeded() shouldBe true
        result?.let { future.result() shouldBe it }
        return future.result()
    }

    fun <T> awaitFailedFuture(future: Future<T>, cause: Throwable? = null, maxDuration: Long = 1000L): Throwable {
        val start = System.currentTimeMillis()
        while(!future.isComplete && System.currentTimeMillis() - start < maxDuration) {
            Thread.sleep(100)
        }
        future.failed() shouldBe true
        cause?.let { future.cause() shouldBe it}
        return future.cause()
    }



}