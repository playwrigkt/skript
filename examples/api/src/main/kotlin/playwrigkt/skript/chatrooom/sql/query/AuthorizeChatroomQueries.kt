package playwrigkt.skript.chatrooom.sql.query

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.sql.SqlCommand
import playwrigkt.skript.sql.SqlQueryMapping
import playwrigkt.skript.sql.SqlResult
import playwrigkt.skript.sql.SqlStatement
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.models.UserError

fun <T> authenticate(): Skript<playwrigkt.skript.auth.TokenAndInput<T>, playwrigkt.skript.auth.SessionAndInput<T>, ApplicationTroupe> {
    return playwrigkt.skript.auth.AuthSkripts.validateAction()
}

fun authorizeChatroomSelectStatement(chatRoomId: String, userId: String, permissionKey: String): SqlStatement.Parameterized {
    return SqlStatement.Parameterized(
            """SELECT NOT exists (
                |SELECT user_id FROM chatroom_user_banned WHERE chatroom_id=? AND user_id=?
            |) AND exists(
                |SELECT user_id
                |FROM chatroom_user_permission
                |WHERE chatroom_id=? AND user_id=? AND permission_key=?
                |UNION
                |SELECT 'anon'
                |FROM chatroom_permission
                |WHERE chatroom_id=? AND permission_key=? AND allow_public=true) AS authorized""".trimMargin(),
            listOf(chatRoomId, userId, chatRoomId, userId, permissionKey, chatRoomId, permissionKey))
}

fun <T> mapAuthResultSet(input: T, rs: SqlResult.Query): Try<T> {
    return Try { rs.result.next() }
            .map { it.getBoolean("authorized") }
            .filter { it }
            .map { input }
            .rescue { Try.Failure<T>(UserError.AuthorizationFailed) }
}


object AuthorizeChatRoomAddUser: SqlQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>): SqlCommand.Query {
        return SqlCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddUser.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, rs: SqlResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeChatRoomRemoveUser: SqlQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>): SqlCommand.Query {
        return SqlCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUser.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, rs: SqlResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorzeChatRoomUpdate: SqlQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>): SqlCommand.Query {
        return SqlCommand.Query(authorizeChatroomSelectStatement(i.input.id, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Update.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>, rs: SqlResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizedGetChatroom : SqlQueryMapping<playwrigkt.skript.auth.SessionAndInput<String>, playwrigkt.skript.auth.SessionAndInput<String>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<String>): SqlCommand.Query {
        return SqlCommand.Query(authorizeChatroomSelectStatement(i.input, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Get.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<String>, rs: SqlResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<String>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthrorizeCreateChatroom : SqlQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>): SqlCommand.Query {
        return SqlCommand.Query(SqlStatement.Simple("SELECT true AS authorized"))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>, rs: SqlResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeAddPublicPermission: SqlQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>): SqlCommand.Query {
        return SqlCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddPublicPermission.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>, rs: SqlResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeRemovePublicPermission: SqlQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>): SqlCommand.Query {
        return SqlCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.RemovePublicPermission.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>, rs: SqlResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeAddUserPermission: SqlQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>): SqlCommand.Query {
        return SqlCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, rs: SqlResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeRemoveUserPermission: SqlQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>): SqlCommand.Query {
        return SqlCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, rs: SqlResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}