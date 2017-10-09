package dev.yn.playground.chatrooom.sql.update

import dev.yn.playground.chatrooom.models.ChatRoom
import dev.yn.playground.chatrooom.models.ChatRoomPermissions
import dev.yn.playground.sql.SQLError
import dev.yn.playground.sql.SQLStatement
import dev.yn.playground.sql.UpdateSQLMapping
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.UpdateResult
import org.funktionale.tries.Try

object InsertChatRoom: UpdateSQLMapping<ChatRoom, ChatRoom> {
    override fun toSql(i: ChatRoom): SQLStatement {
        return SQLStatement.Parameterized("INSERT INTO chatroom (id, name, description) VALUES (?, ?, ?)", JsonArray(listOf(i.id, i.name, i.description)))
    }

    override fun mapResult(i: ChatRoom, rs: UpdateResult): Try<ChatRoom> {
        return if(rs.updated == 1) Try.Success(i) else Try.Failure(SQLError.UpdateFailed(this, i))
    }

}

object InsertChatRoomUsers : UpdateSQLMapping<ChatRoom, ChatRoom> {
    override fun toSql(i: ChatRoom): SQLStatement {
        return SQLStatement.Parameterized(
                "INSERT INTO chatroom_user_permission (chatroom_id, user_id, permission_key, date_added) VALUES ${(1..i.users.map {it.permissions.size }.sum()).map { "(?, ?, ?, transaction_timestamp())" }.joinToString(",")}",
                JsonArray(i.users.flatMap { chatRoomUser ->  chatRoomUser.permissions.flatMap { listOf(chatRoomUser.chatroom.id, chatRoomUser.user.id, it) }}))
    }

    override fun mapResult(i: ChatRoom, rs: UpdateResult): Try<ChatRoom> {
        return if(rs.updated == i.users.map { it.permissions.size }.sum()) Try.Success(i) else Try.Failure(SQLError.UpdateFailed(this, i))
    }

}

object InsertChatRoomPermissions: UpdateSQLMapping<ChatRoom, ChatRoom> {
    override fun toSql(i: ChatRoom): SQLStatement {
        return SQLStatement.Parameterized(
                "INSERT INTO chatroom_permission (chatroom_id, permission_key, allow_public) VALUES ${i.publicPermissions.map { "(?, ?, true)" }.joinToString(",") }",
                JsonArray(i.publicPermissions.flatMap { listOf(i.id, it)}))
    }

    override fun mapResult(i: ChatRoom, rs: UpdateResult): Try<ChatRoom> {
        return if(rs.updated == i.publicPermissions.size) Try.Success(i) else Try.Failure(SQLError.UpdateFailed(this, i))
    }
}