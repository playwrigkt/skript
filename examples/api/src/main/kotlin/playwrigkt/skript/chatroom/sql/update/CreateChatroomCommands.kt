package playwrigkt.skript.chatroom.sql.update

import arrow.core.Try
import playwrigkt.skript.sql.*

object InsertChatRoom: SqlUpdateMapping<playwrigkt.skript.chatroom.models.ChatRoom, playwrigkt.skript.chatroom.models.ChatRoom> {
    override fun toSql(i: playwrigkt.skript.chatroom.models.ChatRoom): SqlCommand.Update {
        return SqlCommand.Update(SqlStatement.Parameterized("INSERT INTO chatroom (id, name, description) VALUES (?, ?, ?)", listOf(i.id, i.name, i.description)))
    }

    override fun mapResult(i: playwrigkt.skript.chatroom.models.ChatRoom, rs: SqlResult.Update): Try<playwrigkt.skript.chatroom.models.ChatRoom> {
        return if(rs.count == 1) Try.Success(i) else Try.Failure(SqlError.UpdateMappingFailed(this, i))
    }

}

object InsertChatRoomUsers : SqlUpdateMapping<playwrigkt.skript.chatroom.models.ChatRoom, playwrigkt.skript.chatroom.models.ChatRoom> {
    override fun toSql(i: playwrigkt.skript.chatroom.models.ChatRoom): SqlCommand.Update {
        return SqlCommand.Update(SqlStatement.Parameterized(
                "INSERT INTO chatroom_user_permission (chatroom_id, user_id, permission_key, date_added) VALUES ${(1..i.users.map {it.permissions.size }.sum()).map { "(?, ?, ?, transaction_timestamp())" }.joinToString(",")}",
                i.users.flatMap { chatRoomUser ->  chatRoomUser.permissions.flatMap { listOf(chatRoomUser.chatroom.id, chatRoomUser.user.id, it) }}))
    }

    override fun mapResult(i: playwrigkt.skript.chatroom.models.ChatRoom, rs: SqlResult.Update): Try<playwrigkt.skript.chatroom.models.ChatRoom> {
        return if(rs.count == i.users.map { it.permissions.size }.sum()) Try.Success(i) else Try.Failure(SqlError.UpdateMappingFailed(this, i))
    }

}

object InsertChatRoomPermissions: SqlUpdateMapping<playwrigkt.skript.chatroom.models.ChatRoom, playwrigkt.skript.chatroom.models.ChatRoom> {
    override fun toSql(i: playwrigkt.skript.chatroom.models.ChatRoom): SqlCommand.Update {
        return SqlCommand.Update(SqlStatement.Parameterized(
                "INSERT INTO chatroom_permission (chatroom_id, permission_key, allow_public) VALUES ${i.publicPermissions.map { "(?, ?, true)" }.joinToString(",") }",
                i.publicPermissions.flatMap { listOf(i.id, it)}))
    }

    override fun mapResult(i: playwrigkt.skript.chatroom.models.ChatRoom, rs: SqlResult.Update): Try<playwrigkt.skript.chatroom.models.ChatRoom> {
        return if(rs.count == i.publicPermissions.size) Try.Success(i) else Try.Failure(SqlError.UpdateMappingFailed(this, i))
    }
}