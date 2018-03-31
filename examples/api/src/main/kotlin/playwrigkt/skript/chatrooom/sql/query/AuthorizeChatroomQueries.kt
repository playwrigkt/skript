package playwrigkt.skript.chatrooom.sql.query

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.common.ApplicationStage
import playwrigkt.skript.sql.SQLCommand
import playwrigkt.skript.sql.SQLQueryMapping
import playwrigkt.skript.sql.SQLResult
import playwrigkt.skript.sql.SQLStatement
import playwrigkt.skript.user.models.UserError

fun <T> authenticate(): Skript<playwrigkt.skript.auth.TokenAndInput<T>, playwrigkt.skript.auth.SessionAndInput<T>, ApplicationStage> {
    return playwrigkt.skript.auth.AuthSkripts.validateAction()
}

fun authorizeChatroomSelectStatement(chatRoomId: String, userId: String, permissionKey: String): SQLStatement.Parameterized {
    return SQLStatement.Parameterized(
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

fun <T> mapAuthResultSet(input: T, rs: SQLResult.Query): Try<T> {
    return Try { rs.result.next() }
            .map { it.getBoolean("authorized") }
            .filter { it }
            .map { input }
            .rescue { Try.Failure<T>(UserError.AuthorizationFailed) }
}


object AuthorizeChatRoomAddUser: SQLQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddUser.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, rs: SQLResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeChatRoomRemoveUser: SQLQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUser.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, rs: SQLResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorzeChatRoomUpdate: SQLQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input.id, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Update.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>, rs: SQLResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizedGetChatroom : SQLQueryMapping<playwrigkt.skript.auth.SessionAndInput<String>, playwrigkt.skript.auth.SessionAndInput<String>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<String>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.Get.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<String>, rs: SQLResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<String>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthrorizeCreateChatroom : SQLQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>): SQLCommand.Query {
        return SQLCommand.Query(SQLStatement.Simple("SELECT true AS authorized"))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>, rs: SQLResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoom>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeAddPublicPermission: SQLQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddPublicPermission.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>, rs: SQLResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeRemovePublicPermission: SQLQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.RemovePublicPermission.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>, rs: SQLResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomPermissions>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeAddUserPermission: SQLQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, rs: SQLResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeRemoveUserPermission: SQLQueryMapping<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
    override fun toSql(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwrigkt.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key))
    }

    override fun mapResult(i: playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>, rs: SQLResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<playwrigkt.skript.chatrooom.models.ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}