package dev.yn.playground.chatrooom.sql

import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.sql.UnpreparedSQLAction

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

    val initAction = UnpreparedSQLAction
            .exec<Unit, ApplicationContextProvider>(createChatRoomTable)
            .exec(createChatRoomPermissionTable)
            .exec(createChatRoomUserPermissionTable)
            .exec(createBannedUserTable)

    val dropAllAction = UnpreparedSQLAction
            .dropTableIfExists<Unit, ApplicationContextProvider>("chatroom_user_banned")
            .dropTableIfExists("chatroom_user_permission")
            .dropTableIfExists("chatroom_permission")
            .dropTableIfExists("chatroom")
}