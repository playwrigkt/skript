package dev.yn.playground.test
//
//import com.sun.org.apache.xpath.internal.operations.Bool
//import dev.yn.playground.sql.*
//import io.vertx.core.json.JsonArray
//import io.vertx.ext.sql.ResultSet
//import io.vertx.ext.sql.UpdateResult
//import org.funktionale.tries.Try
//import java.sql.Timestamp
//import java.time.Instant
//
//object UserSQL {
//    val selectSessionByKey= "SELECT session_key, user_id, expiration FROM user_session where session_key = ?"
//    val selectUser = "SELECT id, user_name, allow_public_message FROM user_profile where id = ?"
//    val selectUserId = "SELECT id from user_profile WHERE user_name = ?"
//    val selectUserPassword = "SELECT user_id FROM user_password WHERE user_id = ? AND pswhash = crypt(?, pswhash)"
//    val selectUserSessionExists = "SELECT exists(select user_id from user_session where user_id = ?) as succeeded"
//    val insertSession = "INSERT INTO user_session (session_key, user_id, expiration) VALUES (?, ?, ?)"
//
//}
//
//object InsertUserProfileMapping: SQLMapping<UserProfileAndPassword, UserProfileAndPassword, SQLCommand.Update, SQLResult.Update> {
//    val insertUser = "INSERT INTO user_profile (id, user_name, allow_public_message) VALUES (?, ?, ?)"
//
//    override fun toSql(i: UserProfileAndPassword): SQLCommand.Update =
//            SQLCommand.Update(SQLStatement.Parameterized(insertUser, listOf(i.userProfile.id, i.userProfile.name, i.userProfile.allowPubliMessage)))
//
//    override fun mapResult(i: UserProfileAndPassword, rs: SQLResult.Update): Try<UserProfileAndPassword> =
//            if(rs.count == 1) Try.Success(i) else Try.Failure(SQLError.UpdateMappingFailed(this, i))
//}
//
//object InsertUserPasswordMapping: SQLMapping<UserProfileAndPassword, UserProfile, SQLCommand.Update, SQLResult.Update> {
//    val insertUserPassword = "INSERT into user_password (user_id, pswhash) VALUES (?, crypt(?, gen_salt('bf')))"
//
//    override fun toSql(i: UserProfileAndPassword): SQLCommand.Update =
//            SQLCommand.Update(SQLStatement.Parameterized(insertUserPassword, listOf(i.userProfile.id, i.password)))
//
//    override fun mapResult(i: UserProfileAndPassword, rs: SQLResult.Update): Try<UserProfile> =
//            if(rs.count== 1) Try.Success(i.userProfile) else Try.Failure(SQLError.UpdateMappingFailed(this, i))
//
//}
//
//object ValidatePasswordForUserId : SQLMapping<UserIdAndPassword, String, SQLCommand.Query, SQLResult.Query> {
//
//    override fun toSql(i: UserIdAndPassword): SQLCommand.Query =
//            SQLCommand.Query(SQLStatement.Parameterized(UserSQL.selectUserPassword, listOf(i.id, i.password)))
//
//    override fun mapResult(i: UserIdAndPassword, rs: SQLResult.Query): Try<String> =
//            rs.result.map { it.get(("user_id")) }
//                    .firstOrNull()
//                    ?.let { Try.Success(it) }
//                    ?.flatMap {
//                        when(it) {
//                            is String -> Try.Success(it)
//                            else -> Try.Failure<String>(RuntimeException("type error: $rs"))
//                        }
//                    }
//                    ?:Try.Failure<String>(UserError.AuthenticationFailed)
//}
//
//object SelectUserIdForLogin : SQLMapping<UserNameAndPassword, UserIdAndPassword, SQLCommand.Query, SQLResult.Query> {
//    override fun toSql(i: UserNameAndPassword): SQLCommand.Query =
//            SQLCommand.Query(SQLStatement.Parameterized(UserSQL.selectUserId, listOf(i.userName)))
//
//
//    override fun mapResult(i: UserNameAndPassword, rs: SQLResult.Query): Try<UserIdAndPassword> =
//            rs.result.map { it.get("id") }
//                    .firstOrNull()
//                    ?.let { Try.Success(it) }
//                    ?.flatMap {
//                        when(it) {
//                            is String -> Try.Success(UserIdAndPassword(it, i.password))
//                            else -> Try.Failure<UserIdAndPassword>(RuntimeException("type error: $rs"))
//                        }
//                    }
//                    ?:Try.Failure<UserIdAndPassword>(UserError.NoSuchUser(i.userName))
//}
//
//object InsertSession: SQLMapping<UserSession, UserSession, SQLCommand.Update, SQLResult.Update> {
//
//    override fun toSql(i: UserSession): SQLCommand.Update =
//            SQLCommand.Update(SQLStatement.Parameterized(UserSQL.insertSession, listOf(i.sessionKey, i.userId, Timestamp.from(i.expiration))))
//
//    override fun mapResult(i: UserSession, rs: SQLResult.Update): Try<UserSession> =
//            if(rs.count == 1) Try.Success(i) else Try.Failure(SQLError.UpdateMappingFailed(this, i))
//}
//
//class SelectSessionByKey<T>(val validateSesssion: (UserSession, T) -> Try<T>): SQLMapping<TokenAndInput<T>, T, SQLCommand.Query, SQLResult.Query> {
//    override fun mapResult(i: TokenAndInput<T>, rs: SQLResult.Query): Try<T> =
//        rs.result
//                .firstOrNull()
//                ?.let {
//                    Try { UserSession(
//                            it.get("session_key") as String,
//                            it.get("user_id") as String,
//                            (it.get("expiration") as Timestamp).toInstant()) } }
//                ?.flatMap {
//                    if(it.expiration.isBefore(Instant.now())) {
//                        Try.Failure<T>(UserError.SessionExpired)
//                    } else {
//                        validateSesssion(it, i.input)
//                    } }
//                ?: Try.Failure<T>(UserError.AuthenticationFailed)
//
//
//    override fun toSql(i: TokenAndInput<T>): SQLCommand.Query=
//            SQLCommand.Query(SQLStatement.Parameterized(UserSQL.selectSessionByKey, listOf(i.token)))
//}
//
//object SelectUserProfileById: SQLMapping<String, UserProfile, SQLCommand.Query, SQLResult.Query> {
//    override fun toSql(i: String): SQLCommand.Query =
//            SQLCommand.Query(SQLStatement.Parameterized(UserSQL.selectUser, listOf(i)))
//
//    override fun mapResult(i: String, rs: SQLResult.Query): Try<UserProfile> =
//            rs.result
//                    .map {
//                        Try { UserProfile(
//                                it.get("id") as String,
//                            it.get("user_name") as String,
//                            it.get("allow_public_message") as Boolean) }
//                    }
//                    .firstOrNull()
//                    ?: Try.Failure<UserProfile>(UserError.NoSuchUser(i))
//}
//
//class SelectUserSessionExists<T>(val onResult:(String, Boolean) -> Try<T>): SQLMapping<String, T, SQLCommand.Query, SQLResult.Query> {
//
//    override fun toSql(i: String): SQLCommand.Query =
//            SQLCommand.Query(SQLStatement.Parameterized(UserSQL.selectUserSessionExists, listOf(i)))
//
//    override fun mapResult(i: String, rs: SQLResult.Query): Try<T> =
//            rs.result
//                    .let {
//                        it.firstOrNull()
//                                ?.let { Try { it.get("succeeded") as Boolean } }
//                                ?: Try.Success(false)
//                    }
//                    .flatMap { onResult(i, it) }
//}
//
//val EnsureNoSessionExists = SelectUserSessionExists({ userId, exists ->
//    if (exists) {
//        Try.Failure<String>(UserError.SessionAlreadyExists(userId))
//    } else {
//        Try.Success(userId)
//    }
//})
//
//object InsertTrustedDevice: SQLMapping<UserTrustedDevice, UserTrustedDevice, SQLCommand.Update, SQLResult.Update> {
//    val insertTrustedDevice = "INSERT INTO user_trusted_device (device_key, user_id, device_name, expiration) VALUES (?, ?, ?, ?)"
//
//    override fun toSql(i: UserTrustedDevice): SQLCommand.Update =
//            SQLCommand.Update(SQLStatement.Parameterized(insertTrustedDevice, listOf(i.deviceKey, i.userId, i.deviceName, Timestamp.from(i.expiration))))
//
//    override fun mapResult(i: UserTrustedDevice, rs: SQLResult.Update): Try<UserTrustedDevice> =
//            if(rs.count == 1) Try.Success(i) else Try.Failure(SQLError.UpdateMappingFailed(this, i))
//}
//
//object SelectUserSessionFromTrustedDevice: SQLMapping<UserTrustedDevice, UserSession, SQLCommand.Query, SQLResult.Query> {
//    val selectUserSessionFromTrustedDevice =
//            "SELECT user_session.session_key, user_session.user_id, user_session.expiration from user_trusted_device " +
//                    "LEFT JOIN user_session on user_trusted_device.user_id = user_session.user_id " +
//                    "WHERE user_trusted_device.device_key=? AND user_trusted_device.user_id=?"
//
//    override fun toSql(i: UserTrustedDevice): SQLCommand.Query =
//            SQLCommand.Query(SQLStatement.Parameterized(selectUserSessionFromTrustedDevice, listOf(i.deviceKey, i.userId)))
//
//    override fun mapResult(i: UserTrustedDevice, rs: SQLResult.Query): Try<UserSession> =
//        rs.result
//                .firstOrNull()
//                ?.let { Try {
//                    UserSession(
//                            it.get("session_key") as String,
//                            it.get("user_id") as String,
//                            (it.get("expiration") as Timestamp).toInstant()) }
//                }
//                ?:Try.Failure(UserError.NoSuchTrustedDevice(i))
//}