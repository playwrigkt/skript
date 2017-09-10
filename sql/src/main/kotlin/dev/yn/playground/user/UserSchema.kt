package dev.yn.playground.user

import dev.yn.playground.sql.SQLTransaction
import dev.yn.playground.sql.extensions.transaction.*

object UserSchema {

    val createUserProfileTable = """CREATE TABLE IF NOT EXISTS user_profile (
    id text PRIMARY KEY,
    name text UNIQUE,
    allow_public_message boolean
);"""

    val createUserPasswordTable = """CREATE TABLE IF NOT EXISTS user_password (
    user_id text REFERENCES user_profile(id) PRIMARY KEY,
    pswhash text
);
"""

    val createUserSessionTable = """CREATE TABLE user_session(
    session_key text PRIMARY KEY,
    user_id text REFERENCES user_profile(id) UNIQUE,
    expiration timestamp
);"""

    val init: SQLTransaction<Unit, Unit, Unit> = exec("CREATE EXTENSION IF NOT EXISTS pgcrypto")
            .exec(createUserProfileTable)
            .exec(createUserPasswordTable)
            .exec(createUserSessionTable)

    val drop: SQLTransaction<Unit, Unit, Unit> = dropTable("user_password")
            .dropTable("user_session")
            .dropTable("user_profile")
}