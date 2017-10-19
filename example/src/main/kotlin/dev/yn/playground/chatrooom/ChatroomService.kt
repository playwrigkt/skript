package dev.yn.playground.chatrooom

import dev.yn.playground.auth.TokenAndInput
import dev.yn.playground.chatrooom.models.ChatRoom
import dev.yn.playground.chatrooom.models.ChatRoomPermissions
import dev.yn.playground.chatrooom.models.ChatRoomUser
import dev.yn.playground.chatrooom.sql.ChatRoomTransactions
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.sql.task.UnpreparedTransactionalSQLTask
import dev.yn.playground.task.Task

class ChatroomService(val provider: ApplicationContextProvider) {
    val addUserTask: Task<TokenAndInput<ChatRoomUser>, ChatRoom> = UnpreparedTransactionalSQLTask(ChatRoomTransactions.addUserTransaction).prepare(provider)
    val removeUserTask: Task<TokenAndInput<ChatRoomUser>, ChatRoom> = UnpreparedTransactionalSQLTask(ChatRoomTransactions.removeUserTransaction).prepare(provider)
    val addUserPermissionsTask: Task<TokenAndInput<ChatRoomUser>, ChatRoom> = UnpreparedTransactionalSQLTask(ChatRoomTransactions.addUserPermissions).prepare(provider)
    val removeUserPermissionsTask: Task<TokenAndInput<ChatRoomUser>, ChatRoom> = UnpreparedTransactionalSQLTask(ChatRoomTransactions.removeUserPermissions).prepare(provider)
    val addPublicPermissionTask: Task<TokenAndInput<ChatRoomPermissions>, ChatRoom> = UnpreparedTransactionalSQLTask(ChatRoomTransactions.addPermissions).prepare(provider)
    val removePublicPermissionTask: Task<TokenAndInput<ChatRoomPermissions>, ChatRoom> = UnpreparedTransactionalSQLTask(ChatRoomTransactions.removePermissions).prepare(provider)
    val createChatRoomTask: Task<TokenAndInput<ChatRoom>, ChatRoom> = UnpreparedTransactionalSQLTask(ChatRoomTransactions.createChatRoomTransaction).prepare(provider)
    val updateChatRoomTask: Task<TokenAndInput<ChatRoom>, ChatRoom> = UnpreparedTransactionalSQLTask(ChatRoomTransactions.updateChatRoomTransaction).prepare(provider)
    val getChatroomTask: Task<TokenAndInput<String>, ChatRoom> = UnpreparedTransactionalSQLTask(ChatRoomTransactions.getChatRoomTransaction).prepare(provider)
}