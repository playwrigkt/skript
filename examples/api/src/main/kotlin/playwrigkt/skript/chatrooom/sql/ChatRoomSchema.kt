package playwrigkt.skript.chatrooom.sql

import playwrigkt.skript.Skript
import playwrigkt.skript.ex.dropTableIfExists
import playwrigkt.skript.ex.exec
import playwrigkt.skript.sql.SQLMapping
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
            .exec(SQLMapping.exec(playwrigkt.skript.chatrooom.sql.ChatRoomSchema.createChatRoomTable))
            .exec(SQLMapping.exec(playwrigkt.skript.chatrooom.sql.ChatRoomSchema.createChatRoomPermissionTable))
            .exec(SQLMapping.exec(playwrigkt.skript.chatrooom.sql.ChatRoomSchema.createChatRoomUserPermissionTable))
            .exec(SQLMapping.exec(playwrigkt.skript.chatrooom.sql.ChatRoomSchema.createBannedUserTable))

    val dropAllAction = Skript.identity<Unit, ApplicationTroupe>()
            .dropTableIfExists("chatroom_user_banned")
            .dropTableIfExists("chatroom_user_permission")
            .dropTableIfExists("chatroom_permission")
            .dropTableIfExists("chatroom")
}