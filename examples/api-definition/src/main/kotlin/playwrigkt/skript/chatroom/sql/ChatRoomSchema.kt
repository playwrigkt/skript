package playwrigkt.skript.chatroom.sql

import playwrigkt.skript.Skript
import playwrigkt.skript.ex.dropTableIfExists
import playwrigkt.skript.ex.exec
import playwrigkt.skript.sql.SqlMapping
import playwrigkt.skript.troupe.ApplicationTroupe

object ChatRoomSchema {
    val createChatRoomTable = """
    CREATE TABLE IF NOT EXISTS chatroom(
        id text PRIMARY KEY,
        name text,
        description text)""".trimIndent()

    val createChatRoomPermissionTable = """
    CREATE TABLE IF NOT EXISTS chatroom_permission(
        chatroom_id text REFERENCES chatroom(id),
        permission_key text,
        allow_public boolean,
        PRIMARY KEY (chatroom_id, permission_key))""".trimIndent()

    val createChatRoomUserPermissionTable = """
    CREATE TABLE IF NOT EXISTS chatroom_user_permission(
        chatroom_id text REFERENCES chatroom(id),
        user_id text REFERENCES user_profile(id),
        permission_key text,
        date_added timestamp,
        PRIMARY KEY (chatroom_id, user_id, permission_key))""".trimIndent()

    val createBannedUserTable = """
    CREATE TABLE IF NOT EXISTS chatroom_user_banned(
        chatroom_id text REFERENCES chatroom(id),
        user_id text REFERENCES user_profile(id),
        date_added timestamp,
        PRIMARY KEY (chatroom_id, user_id))""".trimIndent()

    val initAction = Skript.identity<Unit, ApplicationTroupe>()
            .exec(SqlMapping.exec(playwrigkt.skript.chatroom.sql.ChatRoomSchema.createChatRoomTable))
            .exec(SqlMapping.exec(playwrigkt.skript.chatroom.sql.ChatRoomSchema.createChatRoomPermissionTable))
            .exec(SqlMapping.exec(playwrigkt.skript.chatroom.sql.ChatRoomSchema.createChatRoomUserPermissionTable))
            .exec(SqlMapping.exec(playwrigkt.skript.chatroom.sql.ChatRoomSchema.createBannedUserTable))

    val dropAllAction = Skript.identity<Unit, ApplicationTroupe>()
            .dropTableIfExists("chatroom_user_banned")
            .dropTableIfExists("chatroom_user_permission")
            .dropTableIfExists("chatroom_permission")
            .dropTableIfExists("chatroom")
}