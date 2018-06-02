package playwrigkt.skript.user.sql

import arrow.core.Try
import arrow.core.recoverWith
import playwrigkt.skript.sql.*
import playwrigkt.skript.user.models.*
import java.sql.Timestamp
import java.time.Instant

object UserSql {
    val selectSessionByKey= "SELECT session_key, user_id, expiration FROM user_session where session_key = ?"
}

object InsertUserProfileMapping: SqlUpdateMapping<UserProfileAndPassword, UserProfileAndPassword> {
    val insertUser = "INSERT INTO user_profile (id, user_name, allow_public_message) VALUES (?, ?, ?)"

    override fun toSql(i: UserProfileAndPassword): SqlCommand.Update =
            SqlCommand.Update(SqlStatement.Parameterized(insertUser, listOf(i.userProfile.id, i.userProfile.name, i.userProfile.allowPublicMessage)))

    override fun mapResult(i: UserProfileAndPassword, rs: SqlResult.Update): Try<UserProfileAndPassword> =
            if(rs.count == 1) Try.Success(i) else Try.Failure(SqlError.UpdateMappingFailed(this, i))
}

object InsertUserPasswordMapping: SqlUpdateMapping<UserProfileAndPassword, UserProfile> {
    val insertUserPassword = "INSERT into user_password (user_id, pswhash) VALUES (?, crypt(?, gen_salt('bf')))"

    override fun toSql(i: UserProfileAndPassword): SqlCommand.Update =
            SqlCommand.Update(SqlStatement.Parameterized(insertUserPassword, listOf(i.userProfile.id, i.password)))

    override fun mapResult(i: UserProfileAndPassword, rs: SqlResult.Update): Try<UserProfile> =
            if(rs.count == 1) Try.Success(i.userProfile) else Try.Failure(SqlError.UpdateMappingFailed(this, i))
}

object ValidatePasswordForUserId : SqlQueryMapping<UserIdAndPassword, String> {
    val selectUserPassword = "SELECT user_id FROM user_password WHERE user_id = ? AND pswhash = crypt(?, pswhash)"

    override fun toSql(i: UserIdAndPassword): SqlCommand.Query =
            SqlCommand.Query(SqlStatement.Parameterized(selectUserPassword, listOf(i.id, i.password)))

    override fun mapResult(i: UserIdAndPassword, rs: SqlResult.Query): Try<String> =
            Try { rs.result.iterator().next() }
                    .map { it.getString("user_id") }
                    .recoverWith { Try.Failure(UserError.AuthenticationFailed) }
}

object SelectUserIdForLogin : SqlQueryMapping<UserNameAndPassword, UserIdAndPassword> {
    val selectUserId = "SELECT id from user_profile WHERE user_name = ?"

    override fun toSql(i: UserNameAndPassword): SqlCommand.Query = SqlCommand.Query(SqlStatement.Parameterized(selectUserId, listOf(i.userName)))


    override fun mapResult(i: UserNameAndPassword, rs: SqlResult.Query): Try<UserIdAndPassword> =
            Try { rs.result.iterator().next() }
                    .map { UserIdAndPassword(it.getString("id"), i.password) }
                    .recoverWith { Try.Failure(UserError.NoSuchUser(i.userName)) }
}

object InsertSession: SqlUpdateMapping<UserSession, UserSession> {
    val insertSession = "INSERT INTO user_session (session_key, user_id, expiration) VALUES (?, ?, ?)"

    override fun toSql(i: UserSession): SqlCommand.Update =
            SqlCommand.Update(SqlStatement.Parameterized(insertSession, listOf(i.sessionKey, i.userId, Timestamp.from(i.expiration))))

    override fun mapResult(i: UserSession, rs: SqlResult.Update): Try<UserSession> =
            if(rs.count == 1) Try.Success(i) else Try.Failure(SqlError.UpdateMappingFailed(this, i))
}

class SelectSessionByKey<T>(val validateSesssion: (UserSession, T) -> Try<T>): SqlQueryMapping<playwrigkt.skript.auth.TokenAndInput<T>, T> {
    override fun mapResult(i: playwrigkt.skript.auth.TokenAndInput<T>, rs: SqlResult.Query): Try<T> =
        Try { rs.result.iterator().next() }
                .map {  UserSession(
                        it.getString("session_key"),
                        it.getString("user_id"),
                        it.getInstant("expiration"))
                }
                .flatMap {
                    if(it.expiration.isBefore(Instant.now())) {
                        Try.Failure(UserError.SessionExpired)
                    } else {
                        validateSesssion(it, i.input)
                    } }
                .recoverWith { Try.Failure(UserError.AuthenticationFailed) }


    override fun toSql(i: playwrigkt.skript.auth.TokenAndInput<T>): SqlCommand.Query =
            SqlCommand.Query(SqlStatement.Parameterized(UserSql.selectSessionByKey, listOf(i.token)))
}

object SelectUserProfileById: SqlQueryMapping<String, UserProfile> {
    val selectUser = "SELECT id, user_name, allow_public_message FROM user_profile where id = ?"
    override fun toSql(i: String): SqlCommand.Query = SqlCommand.Query(SqlStatement.Parameterized(selectUser, listOf(i)))

    override fun mapResult(i: String, rs: SqlResult.Query): Try<UserProfile> =
            Try { rs.result.next() }
                    .recoverWith { Try.Failure(UserError.NoSuchUser(i)) }
                    .map {
                        UserProfile(it.getString("id"),
                                it.getString("user_name"),
                                it.getBoolean("allow_public_message")) }

}

class SelectUserSessionExists<T>(val onResult:(String, Boolean) -> Try<T>): SqlQueryMapping<String, T> {
    val selectUserSessionExists = "SELECT exists(select user_id from user_session where user_id = ?) as succeeded"

    override fun toSql(i: String): SqlCommand.Query =
            SqlCommand.Query(SqlStatement.Parameterized(selectUserSessionExists, listOf(i)))

    override fun mapResult(i: String, rs: SqlResult.Query): Try<T> =
            Try { rs.result.next() }
                    .map { it.getBoolean("succeeded") }
                    .flatMap { onResult(i, it) }
}

val EnsureNoSessionExists = SelectUserSessionExists({ userId, exists ->
    if (exists) {
        Try.Failure<String>(UserError.SessionAlreadyExists(userId))
    } else {
        Try.Success(userId)
    }
})

object InsertTrustedDevice: SqlUpdateMapping<UserTrustedDevice, UserTrustedDevice> {
    val insertTrustedDevice = "INSERT INTO user_trusted_device (device_key, user_id, device_name, expiration) VALUES (?, ?, ?, ?)"

    override fun toSql(i: UserTrustedDevice): SqlCommand.Update =
            SqlCommand.Update(SqlStatement.Parameterized(insertTrustedDevice, listOf(i.deviceKey, i.userId, i.deviceName, Timestamp.from(i.expiration))))

    override fun mapResult(i: UserTrustedDevice, rs: SqlResult.Update): Try<UserTrustedDevice> =
            if(rs.count== 1) Try.Success(i) else Try.Failure(SqlError.UpdateMappingFailed(this, i))
}

object SelectUserSessionFromTrustedDevice: SqlQueryMapping<UserTrustedDevice, UserSession> {
    val selectUserSessionFromTrustedDevice =
            "SELECT user_session.session_key, user_session.user_id, user_session.expiration from user_trusted_device " +
                    "LEFT JOIN user_session on user_trusted_device.user_id = user_session.user_id " +
                    "WHERE user_trusted_device.device_key=? AND user_trusted_device.user_id=?"

    override fun toSql(i: UserTrustedDevice): SqlCommand.Query =
            SqlCommand.Query(SqlStatement.Parameterized(selectUserSessionFromTrustedDevice, listOf(i.deviceKey, i.userId)))

    override fun mapResult(i: UserTrustedDevice, rs: SqlResult.Query): Try<UserSession> =
        Try { rs.result.next() }
                .recoverWith { Try.Failure(UserError.NoSuchTrustedDevice(i)) }
                .map {
                    UserSession(
                            it.getString("session_key"),
                            it.getString("user_id"),
                            it.getInstant("expiration"))
                }
}