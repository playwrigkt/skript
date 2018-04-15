package playwrigkt.skript.chatrooom.sql.update

import org.funktionale.tries.Try
import playwrigkt.skript.sql.*

object InsertChatRoom: SQLUpdateMapping<playwrigkt.skript.chatrooom.models.ChatRoom, playwrigkt.skript.chatrooom.models.ChatRoom> {
    override fun toSql(i: playwrigkt.skript.chatrooom.models.ChatRoom): SQLCommand.Update {
        return SQLCommand.Update(SQLStatement.Parameterized("INSERT INTO chatroom (id, name, description) VALUES (?, ?, ?)", listOf(i.id, i.name, i.description)))
    }

    override fun mapResult(i: playwrigkt.skript.chatrooom.models.ChatRoom, rs: SQLResult.Update): Try<playwrigkt.skript.chatrooom.models.ChatRoom> {
        return if(rs.count == 1) Try.Success(i) else Try.Failure(SQLError.UpdateMappingFailed(this, i))
    }

}

object InsertChatRoomUsers : SQLUpdateMapping<playwrigkt.skript.chatrooom.models.ChatRoom, playwrigkt.skript.chatrooom.models.ChatRoom> {
    override fun toSql(i: playwrigkt.skript.chatrooom.models.ChatRoom): SQLCommand.Update {
        return SQLCommand.Update(SQLStatement.Parameterized(
                "INSERT INTO chatroom_user_permission (chatroom_id, user_id, permission_key, date_added) VALUES ${(1..i.users.map {it.permissions.size }.sum()).map { "(?, ?, ?, transaction_timestamp())" }.joinToString(",")}",
                i.users.flatMap { chatRoomUser ->  chatRoomUser.permissions.flatMap { listOf(chatRoomUser.chatroom.id, chatRoomUser.user.id, it) }}))
    }

    override fun mapResult(i: playwrigkt.skript.chatrooom.models.ChatRoom, rs: SQLResult.Update): Try<playwrigkt.skript.chatrooom.models.ChatRoom> {
        return if(rs.count == i.users.map { it.permissions.size }.sum()) Try.Success(i) else Try.Failure(SQLError.UpdateMappingFailed(this, i))
    }

}

object InsertChatRoomPermissions: SQLUpdateMapping<playwrigkt.skript.chatrooom.models.ChatRoom, playwrigkt.skript.chatrooom.models.ChatRoom> {
    override fun toSql(i: playwrigkt.skript.chatrooom.models.ChatRoom): SQLCommand.Update {
        return SQLCommand.Update(SQLStatement.Parameterized(
                "INSERT INTO chatroom_permission (chatroom_id, permission_key, allow_public) VALUES ${i.publicPermissions.map { "(?, ?, true)" }.joinToString(",") }",
                i.publicPermissions.flatMap { listOf(i.id, it)}))
    }

    override fun mapResult(i: playwrigkt.skript.chatrooom.models.ChatRoom, rs: SQLResult.Update): Try<playwrigkt.skript.chatrooom.models.ChatRoom> {
        return if(rs.count == i.publicPermissions.size) Try.Success(i) else Try.Failure(SQLError.UpdateMappingFailed(this, i))
    }
}