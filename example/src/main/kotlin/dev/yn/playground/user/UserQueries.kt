package dev.yn.playground.user

import dev.yn.playground.sql.QuerySQLMapping
import dev.yn.playground.sql.SQLError
import dev.yn.playground.sql.SQLStatement
import dev.yn.playground.sql.UpdateSQLMapping
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.UpdateResult
import org.funktionale.tries.Try
import java.sql.Timestamp
import java.time.Instant

object UserSQL {
    val selectSessionByKey= "SELECT session_key, user_id, expiration FROM user_session where session_key = ?"

}
object InsertUserProfileMapping: UpdateSQLMapping<UserProfileAndPassword, UserProfileAndPassword> {
    val insertUser = "INSERT INTO user_profile (id, user_name, allow_public_message) VALUES (?, ?, ?)"

    override fun toSql(i: UserProfileAndPassword): SQLStatement =
            SQLStatement.Parameterized(insertUser, JsonArray(listOf(i.userProfile.id, i.userProfile.name, i.userProfile.allowPubliMessage)))

    override fun mapResult(i: UserProfileAndPassword, rs: UpdateResult): Try<UserProfileAndPassword> =
            if(rs.updated == 1) Try.Success(i) else Try.Failure(SQLError.UpdateFailed(this, i))
}

object InsertUserPasswordMapping: UpdateSQLMapping<UserProfileAndPassword, UserProfile> {
    val insertUserPassword = "INSERT into user_password (user_id, pswhash) VALUES (?, crypt(?, gen_salt('bf')))"

    override fun toSql(i: UserProfileAndPassword): SQLStatement =
            SQLStatement.Parameterized(insertUserPassword, JsonArray(listOf(i.userProfile.id, i.password)))

    override fun mapResult(i: UserProfileAndPassword, rs: UpdateResult): Try<UserProfile> =
            if(rs.updated == 1) Try.Success(i.userProfile) else Try.Failure(SQLError.UpdateFailed(this, i))

}

object ValidatePasswordForUserId : QuerySQLMapping<UserIdAndPassword, String> {
    val selectUserPassword = "SELECT user_id FROM user_password WHERE user_id = ? AND pswhash = crypt(?, pswhash)"

    override fun toSql(i: UserIdAndPassword): SQLStatement =
            SQLStatement.Parameterized(selectUserPassword, JsonArray(listOf(i.id, i.password)))

    override fun mapResult(i: UserIdAndPassword, rs: ResultSet): Try<String> =
            rs.rows.map { it.getString(("user_id")) }
                    .firstOrNull()
                    ?.let { Try.Success(it) }
                    ?:Try.Failure<String>(UserError.AuthenticationFailed)
}

object SelectUserIdForLogin : QuerySQLMapping<UserNameAndPassword, UserIdAndPassword> {
    val selectUserId = "SELECT id from user_profile WHERE user_name = ?"

    override fun toSql(i: UserNameAndPassword): SQLStatement = SQLStatement.Parameterized(selectUserId, JsonArray(listOf(i.userName)))


    override fun mapResult(i: UserNameAndPassword, rs: ResultSet): Try<UserIdAndPassword> =
            rs.rows.map { UserIdAndPassword(it.getString("id"), i.password) }
                    .firstOrNull()
                    ?.let { Try.Success(it) }
                    ?:Try.Failure<UserIdAndPassword>(UserError.NoSuchUser(i.userName))
}

object InsertSession: UpdateSQLMapping<UserSession, UserSession> {
    val insertSession = "INSERT INTO user_session (session_key, user_id, expiration) VALUES (?, ?, ?)"

    override fun toSql(i: UserSession): SQLStatement =
        SQLStatement.Parameterized(insertSession, JsonArray(listOf(i.sessionKey, i.userId, Timestamp.from(i.expiration))))

    override fun mapResult(i: UserSession, rs: UpdateResult): Try<UserSession> =
            if(rs.updated == 1) Try.Success(i) else Try.Failure(SQLError.UpdateFailed(this, i))
}

class SelectSessionByKey<T>(val validateSesssion: (UserSession, T) -> Try<T>): QuerySQLMapping<TokenAndInput<T>, T> {
    override fun mapResult(i: TokenAndInput<T>, rs: ResultSet): Try<T> =
        rs.rows
                .firstOrNull()
                ?.let { Try { UserSession(it.getString("session_key"), it.getString("user_id"), it.getInstant("expiration")) } }
                ?.flatMap {
                    if(it.expiration.isBefore(Instant.now())) {
                        Try.Failure<T>(UserError.SessionExpired)
                    } else {
                        validateSesssion(it, i.input)
                    } }
                ?: Try.Failure<T>(UserError.AuthenticationFailed)


    override fun toSql(i: TokenAndInput<T>): SQLStatement =
        SQLStatement.Parameterized(UserSQL.selectSessionByKey, JsonArray(listOf(i.token)))
}

object SelectUserProfileById: QuerySQLMapping<String, UserProfile> {
    val selectUser = "SELECT id, user_name, allow_public_message FROM user_profile where id = ?"
    override fun toSql(i: String): SQLStatement =SQLStatement.Parameterized(selectUser, JsonArray(listOf(i)))

    override fun mapResult(i: String, rs: ResultSet): Try<UserProfile> =
            rs.rows.map { Try { UserProfile(it.getString("id"), it.getString("user_name"), it.getBoolean("allow_public_message")) } }
                .firstOrNull()
                ?: Try.Failure<UserProfile>(UserError.NoSuchUser(i))
}

class SelectUserSessionExists<T>(val onResult:(String, Boolean) -> Try<T>): QuerySQLMapping<String, T> {
    val selectUserSessionExists = "SELECT exists(select user_id from user_session where user_id = ?) as succeeded"

    override fun toSql(i: String): SQLStatement =
        SQLStatement.Parameterized(selectUserSessionExists, JsonArray(listOf(i)))

    override fun mapResult(i: String, rs: ResultSet): Try<T> =
            rs.rows
                    .let {
                        it.firstOrNull()
                                ?.let { Try { it.getBoolean("succeeded") } }
                                ?: Try.Success(false)
                    }
                    .flatMap { onResult(i, it) }
}

val EnsureNoSessionExists = SelectUserSessionExists({ userId, exists ->
    if (exists) {
        Try.Failure<String>(UserError.SessionAlreadyExists(userId))
    } else {
        Try.Success(userId)
    }
})

object InsertTrustedDevice: UpdateSQLMapping<UserTrustedDevice, UserTrustedDevice> {
    val insertTrustedDevice = "INSERT INTO user_trusted_device (device_key, user_id, device_name, expiration) VALUES (?, ?, ?, ?)"

    override fun toSql(i: UserTrustedDevice): SQLStatement =
        SQLStatement.Parameterized(insertTrustedDevice, JsonArray(listOf(i.deviceKey, i.userId, i.deviceName, Timestamp.from(i.expiration))))

    override fun mapResult(i: UserTrustedDevice, rs: UpdateResult): Try<UserTrustedDevice> =
            if(rs.updated == 1) Try.Success(i) else Try.Failure(SQLError.UpdateFailed(this, i))
}

object SelectUserSessionFromTrustedDevice: QuerySQLMapping<UserTrustedDevice, UserSession> {
    val selectUserSessionFromTrustedDevice =
            "SELECT user_session.session_key, user_session.user_id, user_session.expiration from user_trusted_device " +
                    "LEFT JOIN user_session on user_trusted_device.user_id = user_session.user_id " +
                    "WHERE user_trusted_device.device_key=? AND user_trusted_device.user_id=?"

    override fun toSql(i: UserTrustedDevice): SQLStatement =
        SQLStatement.Parameterized(selectUserSessionFromTrustedDevice, JsonArray(listOf(i.deviceKey, i.userId)))

    override fun mapResult(i: UserTrustedDevice, rs: ResultSet): Try<UserSession> =
        rs.rows
                .firstOrNull()
                ?.let { Try { UserSession(it.getString("session_key"), it.getString("user_id"), it.getInstant("expiration")) } }
                ?:Try.Failure(UserError.NoSuchTrustedDevice(i))
}