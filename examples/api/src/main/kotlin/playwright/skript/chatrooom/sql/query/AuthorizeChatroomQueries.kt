package playwright.skript.chatrooom.sql.query

import org.funktionale.tries.Try
import playwright.skript.Skript
import playwright.skript.common.ApplicationStage
import playwright.skript.sql.SQLCommand
import playwright.skript.sql.SQLQueryMapping
import playwright.skript.sql.SQLResult
import playwright.skript.sql.SQLStatement
import playwright.skript.user.models.UserError

fun <T> authenticate(): Skript<playwright.skript.auth.TokenAndInput<T>, playwright.skript.auth.SessionAndInput<T>, ApplicationStage> {
    return playwright.skript.auth.AuthSkripts.validateAction()
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


object AuthorizeChatRoomAddUser: SQLQueryMapping<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>> {
    override fun toSql(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddUser.key))
    }

    override fun mapResult(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, rs: SQLResult.Query): Try<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeChatRoomRemoveUser: SQLQueryMapping<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>> {
    override fun toSql(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwright.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUser.key))
    }

    override fun mapResult(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, rs: SQLResult.Query): Try<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorzeChatRoomUpdate: SQLQueryMapping<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoom>, playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoom>> {
    override fun toSql(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoom>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input.id, i.session.userId, playwright.skript.chatrooom.models.ChatRoomPermissionKey.Update.key))
    }

    override fun mapResult(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoom>, rs: SQLResult.Query): Try<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoom>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizedGetChatroom : SQLQueryMapping<playwright.skript.auth.SessionAndInput<String>, playwright.skript.auth.SessionAndInput<String>> {
    override fun toSql(i: playwright.skript.auth.SessionAndInput<String>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input, i.session.userId, playwright.skript.chatrooom.models.ChatRoomPermissionKey.Get.key))
    }

    override fun mapResult(i: playwright.skript.auth.SessionAndInput<String>, rs: SQLResult.Query): Try<playwright.skript.auth.SessionAndInput<String>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthrorizeCreateChatroom : SQLQueryMapping<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoom>, playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoom>> {
    override fun toSql(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoom>): SQLCommand.Query {
        return SQLCommand.Query(SQLStatement.Simple("SELECT true AS authorized"))
    }

    override fun mapResult(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoom>, rs: SQLResult.Query): Try<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoom>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeAddPublicPermission: SQLQueryMapping<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomPermissions>, playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomPermissions>> {
    override fun toSql(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomPermissions>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddPublicPermission.key))
    }

    override fun mapResult(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomPermissions>, rs: SQLResult.Query): Try<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomPermissions>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeRemovePublicPermission: SQLQueryMapping<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomPermissions>, playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomPermissions>> {
    override fun toSql(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomPermissions>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwright.skript.chatrooom.models.ChatRoomPermissionKey.RemovePublicPermission.key))
    }

    override fun mapResult(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomPermissions>, rs: SQLResult.Query): Try<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomPermissions>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeAddUserPermission: SQLQueryMapping<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>> {
    override fun toSql(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwright.skript.chatrooom.models.ChatRoomPermissionKey.AddUserPermission.key))
    }

    override fun mapResult(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, rs: SQLResult.Query): Try<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeRemoveUserPermission: SQLQueryMapping<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>> {
    override fun toSql(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>): SQLCommand.Query {
        return SQLCommand.Query(authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, playwright.skript.chatrooom.models.ChatRoomPermissionKey.RemoveUserPermission.key))
    }

    override fun mapResult(i: playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>, rs: SQLResult.Query): Try<playwright.skript.auth.SessionAndInput<playwright.skript.chatrooom.models.ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}