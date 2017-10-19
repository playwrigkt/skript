package dev.yn.playground.chatrooom.sql.query

import dev.yn.playground.auth.SessionAndInput
import dev.yn.playground.auth.TokenAndInput
import dev.yn.playground.auth.sql.AuthSQLActions
import dev.yn.playground.chatrooom.models.ChatRoom
import dev.yn.playground.chatrooom.models.ChatRoomPermissionKey
import dev.yn.playground.chatrooom.models.ChatRoomPermissions
import dev.yn.playground.chatrooom.models.ChatRoomUser
import dev.yn.playground.sql.QuerySQLMapping
import dev.yn.playground.sql.SQLStatement
import dev.yn.playground.sql.UnpreparedSQLAction
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.user.models.UserError
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.ResultSet
import org.funktionale.tries.Try

fun <T> authenticate(): UnpreparedSQLAction<TokenAndInput<T>, SessionAndInput<T>, ApplicationContextProvider> {
    return AuthSQLActions.validateAction()
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
            JsonArray(listOf(chatRoomId, userId, chatRoomId, userId, permissionKey, chatRoomId, permissionKey)))
}

fun <T> mapAuthResultSet(input: T, rs: ResultSet): Try<T> {
    if(rs.rows.firstOrNull()?.getBoolean("authorized")?:false) {
        return Try.Success(input)
    } else {
        return Try.Failure(UserError.AuthorizationFailed)
    }
}


object AuthorizeChatRoomAddUser: QuerySQLMapping<SessionAndInput<ChatRoomUser>, SessionAndInput<ChatRoomUser>> {
    override fun toSql(i: SessionAndInput<ChatRoomUser>): SQLStatement {
        return authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, ChatRoomPermissionKey.AddUser.key)
    }

    override fun mapResult(i: SessionAndInput<ChatRoomUser>, rs: ResultSet): Try<SessionAndInput<ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeChatRoomRemoveUser: QuerySQLMapping<SessionAndInput<ChatRoomUser>, SessionAndInput<ChatRoomUser>> {
    override fun toSql(i: SessionAndInput<ChatRoomUser>): SQLStatement {
        return authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, ChatRoomPermissionKey.RemoveUser.key)
    }

    override fun mapResult(i: SessionAndInput<ChatRoomUser>, rs: ResultSet): Try<SessionAndInput<ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorzeChatRoomUpdate: QuerySQLMapping<SessionAndInput<ChatRoom>, SessionAndInput<ChatRoom>> {
    override fun toSql(i: SessionAndInput<ChatRoom>): SQLStatement {
        return authorizeChatroomSelectStatement(i.input.id, i.session.userId, ChatRoomPermissionKey.Update.key)
    }

    override fun mapResult(i: SessionAndInput<ChatRoom>, rs: ResultSet): Try<SessionAndInput<ChatRoom>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizedGetChatroom : QuerySQLMapping<SessionAndInput<String>, SessionAndInput<String>> {
    override fun toSql(i: SessionAndInput<String>): SQLStatement {
        return authorizeChatroomSelectStatement(i.input, i.session.userId, ChatRoomPermissionKey.Get.key)
    }

    override fun mapResult(i: SessionAndInput<String>, rs: ResultSet): Try<SessionAndInput<String>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthrorizeCreateChatroom : QuerySQLMapping<SessionAndInput<ChatRoom>, SessionAndInput<ChatRoom>> {
    override fun toSql(i: SessionAndInput<ChatRoom>): SQLStatement {
        return SQLStatement.Simple("SELECT true AS authorized")
    }

    override fun mapResult(i: SessionAndInput<ChatRoom>, rs: ResultSet): Try<SessionAndInput<ChatRoom>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeAddPublicPermission: QuerySQLMapping<SessionAndInput<ChatRoomPermissions>, SessionAndInput<ChatRoomPermissions>> {
    override fun toSql(i: SessionAndInput<ChatRoomPermissions>): SQLStatement {
        return authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, ChatRoomPermissionKey.AddPublicPermission.key)
    }

    override fun mapResult(i: SessionAndInput<ChatRoomPermissions>, rs: ResultSet): Try<SessionAndInput<ChatRoomPermissions>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeRemovePublicPermission: QuerySQLMapping<SessionAndInput<ChatRoomPermissions>, SessionAndInput<ChatRoomPermissions>> {
    override fun toSql(i: SessionAndInput<ChatRoomPermissions>): SQLStatement {
        return authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, ChatRoomPermissionKey.RemovePublicPermission.key)
    }

    override fun mapResult(i: SessionAndInput<ChatRoomPermissions>, rs: ResultSet): Try<SessionAndInput<ChatRoomPermissions>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeAddUserPermission: QuerySQLMapping<SessionAndInput<ChatRoomUser>, SessionAndInput<ChatRoomUser>> {
    override fun toSql(i: SessionAndInput<ChatRoomUser>): SQLStatement {
        return authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, ChatRoomPermissionKey.AddUserPermission.key)
    }

    override fun mapResult(i: SessionAndInput<ChatRoomUser>, rs: ResultSet): Try<SessionAndInput<ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}

object AuthorizeRemoveUserPermission: QuerySQLMapping<SessionAndInput<ChatRoomUser>, SessionAndInput<ChatRoomUser>> {
    override fun toSql(i: SessionAndInput<ChatRoomUser>): SQLStatement {
        return authorizeChatroomSelectStatement(i.input.chatroom.id, i.session.userId, ChatRoomPermissionKey.RemoveUserPermission.key)
    }

    override fun mapResult(i: SessionAndInput<ChatRoomUser>, rs: ResultSet): Try<SessionAndInput<ChatRoomUser>> {
        return mapAuthResultSet(i, rs)
    }
}