package playwrigkt.skript.chatroom.sql.query

import arrow.core.Try
import arrow.core.getOrElse
import playwrigkt.skript.common.models.Reference
import playwrigkt.skript.sql.*
import playwrigkt.skript.user.models.UserProfile


object GetChatRoom: SqlQueryMapping<String, playwrigkt.skript.chatroom.models.ChatRoom> {
    val selectChatroom = """
        |SELECT chatroom.id AS id, chatroom.name AS name, chatroom.description AS description,
        |chatroom_permission.permission_key AS permission_key, chatroom_permission.allow_public AS permission_allow_public,
        |NULL AS user_id, NULL AS user_permission_key, NULL AS user_permission_date,
        |NULL AS user_name, NULL AS user_allow_public_message
        |FROM chatroom
        |LEFT JOIN chatroom_permission
        |ON chatroom.id=chatroom_permission.chatroom_id
        |WHERE chatroom.id=?
        |UNION SELECT chatroom.id AS id, chatroom.name AS name, chatroom.description AS description,
        |NULL AS permission_key, NULL AS permission_allow_public,
        |chatroom_user_permission.user_id AS user_id, chatroom_user_permission.permission_key AS user_permission_key, chatroom_user_permission.date_added AS user_permission_date,
        |user_profile.user_name as user_name, user_profile.allow_public_message AS user_allow_public_message
        |FROM chatroom
        |JOIN chatroom_user_permission
        |JOIN user_profile on chatroom_user_permission.user_id = user_profile.id
        |ON chatroom.id = chatroom_user_permission.chatroom_id
        |WHERE chatroom.id=?
    """.trimMargin()

    override fun toSql(i: String): SqlCommand.Query {
        return SqlCommand.Query(SqlStatement.Parameterized(selectChatroom, listOf(i, i)))
    }

    override fun mapResult(i: String, rs: SqlResult.Query): Try<playwrigkt.skript.chatroom.models.ChatRoom> {
        return Try { rs.result.next() }
                .flatMap(this::parseFirstRow)
                .flatMap { rs.result.asSequence().fold(Try.Success(it)) { agg: Try<playwrigkt.skript.chatroom.models.ChatRoom>, nextRow: SqlRow -> agg.map { chatRoom ->
                    chatRoom.copy(
                            users = parseUser(nextRow).map { addTo(chatRoom.users, it) }.getOrElse { chatRoom.users },
                            publicPermissions = parsePermission(nextRow).map { chatRoom.publicPermissions.plus(it) }.getOrElse { chatRoom.publicPermissions }
                    )
                }} }
    }

    private fun parseFirstRow(row: SqlRow): Try<playwrigkt.skript.chatroom.models.ChatRoom> {
        return Try {
            playwrigkt.skript.chatroom.models.ChatRoom(
                    id = row.getString("id"),
                    name = row.getString("name"),
                    description = row.getString("description"),
                    users = parseUser(row).map(::setOf).getOrElse { emptySet() },
                    publicPermissions = parsePermission(row).map(::setOf).getOrElse { emptySet() })
        }
    }
    private fun parsePermission(row: SqlRow): Try<String> {
        return Try { row.getString("permission_key") }
    }

    private fun parseUser(row: SqlRow): Try<playwrigkt.skript.chatroom.models.ChatRoomUser> {
        return Try {
            playwrigkt.skript.chatroom.models.ChatRoomUser(
                    user = Reference.defined(
                            id = row.getString("user_id"),
                            referenced = UserProfile(
                                    row.getString("user_id"),
                                    row.getString("user_name"),
                                    row.getBoolean("user_allow_public_message"))),
                    chatroom = Reference.empty(id = row.getString("id")),
                    permissions = setOf(row.getString("user_permission_key")))
        }
    }

    private fun addTo(users: Set<playwrigkt.skript.chatroom.models.ChatRoomUser>, chatRoomUser: playwrigkt.skript.chatroom.models.ChatRoomUser):Set<playwrigkt.skript.chatroom.models.ChatRoomUser> {
        return users.plus(chatRoomUser).fold(emptyMap<String, playwrigkt.skript.chatroom.models.ChatRoomUser>()) { agg, nextUser ->
            agg.plus(nextUser.user.id to (agg.get(nextUser.user.id)?.let { it.copy(permissions = it.permissions + nextUser.permissions) }?:nextUser))
        }.values.toSet()
    }
}
