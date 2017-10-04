package dev.yn.playground.chatrooom.sql.update

import dev.yn.playground.chatrooom.models.ChatRoom
import dev.yn.playground.chatrooom.models.ChatRoomUser
import dev.yn.playground.sql.SQLError
import dev.yn.playground.sql.SQLStatement
import dev.yn.playground.sql.UpdateSQLMapping
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.UpdateResult
import org.funktionale.tries.Try

object InsertChatRoomUser: UpdateSQLMapping<ChatRoomUser, ChatRoomUser> {
    override fun toSql(i: ChatRoomUser): SQLStatement {
        return SQLStatement.Parameterized(
                "INSERT INTO chatroom_user_permission (chatroom_id, user_id, permission_key, date_added) VALUES ${(1..i.permissions.size).map {"(?, ?, ?, transaction_timestamp())"}.joinToString(",")}",
                JsonArray(i.permissions.flatMap { listOf(i.chatroom.id, i.user.id, it) }))
    }

    override fun mapResult(i: ChatRoomUser, rs: UpdateResult): Try<ChatRoomUser> {
        return if(rs.updated == i.permissions.size) Try.Success(i) else Try.Failure(SQLError.UpdateFailed(this, i))
    }
}

object DeleteChatRoomUserPermission : UpdateSQLMapping<ChatRoomUser, ChatRoomUser> {
    override fun toSql(i: ChatRoomUser): SQLStatement {
        return SQLStatement.Parameterized(
                "DELETE FROM chatroom_user_permission WHERE chatroom_id=? AND user_id=? AND (${i.permissions.map { "permission_key = ?"}.joinToString(" OR ")})",
                JsonArray(listOf(i.chatroom.id, i.user.id) + i.permissions))
    }

    override fun mapResult(i: ChatRoomUser, rs: UpdateResult): Try<ChatRoomUser> {
        return if(rs.updated == i.permissions.size) Try.Success(i) else Try.Failure(SQLError.UpdateFailed(this, i))
    }
}

object DeleteChatRoomPermissions: UpdateSQLMapping<ChatRoom, ChatRoom> {
    override fun toSql(i: ChatRoom): SQLStatement {
        return SQLStatement.Parameterized(
                "DELETE FROM chatroom_permission WHERE chatroom_id=? AND (${i.publicPermissions.map { "permission_key = ?"}.joinToString(" OR ")})",
        JsonArray(listOf(i.id) + i.publicPermissions))
    }

    override fun mapResult(i: ChatRoom, rs: UpdateResult): Try<ChatRoom> {
        return if(rs.updated == i.publicPermissions.size) Try.Success(i) else Try.Failure(SQLError.UpdateFailed(this, i))
    }

}

object UpdateChatRoomFields: UpdateSQLMapping<ChatRoom, ChatRoom> {
    override fun mapResult(i: ChatRoom, rs: UpdateResult): Try<ChatRoom> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toSql(i: ChatRoom): SQLStatement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}