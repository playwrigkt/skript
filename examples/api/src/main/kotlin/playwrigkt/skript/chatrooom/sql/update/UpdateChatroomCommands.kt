package playwrigkt.skript.chatrooom.sql.update

import org.funktionale.tries.Try
import playwrigkt.skript.sql.*

object InsertChatRoomUserPermissions : SQLUpdateMapping<playwrigkt.skript.chatrooom.models.ChatRoomUser, playwrigkt.skript.chatrooom.models.ChatRoomUser> {
    override fun toSql(i: playwrigkt.skript.chatrooom.models.ChatRoomUser): SQLCommand.Update {
        return SQLCommand.Update(SQLStatement.Parameterized(
                "INSERT INTO chatroom_user_permission (chatroom_id, user_id, permission_key, date_added) VALUES ${(1..i.permissions.size).map {"(?, ?, ?, transaction_timestamp())"}.joinToString(",")}",
                i.permissions.flatMap { listOf(i.chatroom.id, i.user.id, it) }))
    }

    override fun mapResult(i: playwrigkt.skript.chatrooom.models.ChatRoomUser, rs: SQLResult.Update): Try<playwrigkt.skript.chatrooom.models.ChatRoomUser> {
        return if(rs.count == i.permissions.size) Try.Success(i) else Try.Failure(SQLError.UpdateMappingFailed(this, i))
    }
}

object DeleteChatRoomUserPermissions : SQLUpdateMapping<playwrigkt.skript.chatrooom.models.ChatRoomUser, playwrigkt.skript.chatrooom.models.ChatRoomUser> {
    override fun toSql(i: playwrigkt.skript.chatrooom.models.ChatRoomUser): SQLCommand.Update {
        return SQLCommand.Update(SQLStatement.Parameterized(
                "DELETE FROM chatroom_user_permission WHERE chatroom_id=? AND user_id=? AND (${i.permissions.map { "permission_key = ?"}.joinToString(" OR ")})",
                listOf(i.chatroom.id, i.user.id) + i.permissions))
    }

    override fun mapResult(i: playwrigkt.skript.chatrooom.models.ChatRoomUser, rs: SQLResult.Update): Try<playwrigkt.skript.chatrooom.models.ChatRoomUser> {
        return if(rs.count == i.permissions.size) Try.Success(i) else Try.Failure(SQLError.UpdateMappingFailed(this, i))
    }
}

object DeleteChatRoomPermissions: SQLUpdateMapping<playwrigkt.skript.chatrooom.models.ChatRoomPermissions, playwrigkt.skript.chatrooom.models.ChatRoomPermissions> {
    override fun toSql(i: playwrigkt.skript.chatrooom.models.ChatRoomPermissions): SQLCommand.Update {
        return SQLCommand.Update(SQLStatement.Parameterized(
                "DELETE FROM chatroom_permission WHERE chatroom_id=? AND (${i.publicPermissions.map { "permission_key = ?"}.joinToString(" OR ")})",
        listOf(i.chatroom.id) + i.publicPermissions))
    }

    override fun mapResult(i: playwrigkt.skript.chatrooom.models.ChatRoomPermissions, rs: SQLResult.Update): Try<playwrigkt.skript.chatrooom.models.ChatRoomPermissions> {
        return if(rs.count== i.publicPermissions.size) Try.Success(i) else Try.Failure(SQLError.UpdateMappingFailed(this, i))
    }

}

object UpdateChatRoomFields: SQLUpdateMapping<playwrigkt.skript.chatrooom.models.ChatRoom, playwrigkt.skript.chatrooom.models.ChatRoom> {
    override fun mapResult(i: playwrigkt.skript.chatrooom.models.ChatRoom, rs: SQLResult.Update): Try<playwrigkt.skript.chatrooom.models.ChatRoom> {
        return if(rs.count== 1) Try.Success(i) else Try.Failure(SQLError.UpdateMappingFailed(this, i))
    }

    override fun toSql(i: playwrigkt.skript.chatrooom.models.ChatRoom): SQLCommand.Update {
        return SQLCommand.Update(SQLStatement.Parameterized(
                "UPDATE chatroom SET name=?, description=? WHERE id=?",
                listOf(i.name, i.description, i.id)))
    }
}

object AddChatRoomPermissions : SQLUpdateMapping<playwrigkt.skript.chatrooom.models.ChatRoomPermissions, playwrigkt.skript.chatrooom.models.ChatRoomPermissions> {
    override fun toSql(i: playwrigkt.skript.chatrooom.models.ChatRoomPermissions): SQLCommand.Update {
        return SQLCommand.Update(SQLStatement.Parameterized(
                "INSERT INTO chatroom_permission (chatroom_id, permission_key, allow_public) VALUES ${i.publicPermissions.map { "(?, ?, true)" }.joinToString(",") }",
                i.publicPermissions.flatMap { listOf(i.chatroom.id, it)}))
    }

    override fun mapResult(i: playwrigkt.skript.chatrooom.models.ChatRoomPermissions, rs: SQLResult.Update): Try<playwrigkt.skript.chatrooom.models.ChatRoomPermissions> {
        return if(rs.count == i.publicPermissions.size) Try.Success(i) else Try.Failure(SQLError.UpdateMappingFailed(this, i))
    }
}