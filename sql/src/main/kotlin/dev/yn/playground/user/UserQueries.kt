package dev.yn.playground.user

import dev.yn.playground.sql.QuerySQLMapping
import dev.yn.playground.sql.SQLStatement
import dev.yn.playground.sql.UpdateSQLMapping
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.UpdateResult
import org.funktionale.tries.Try

object InsertUserProfileMapping: UpdateSQLMapping<UserAndPassword, UserAndPassword> {
    val insertUser = "INSERT INTO user_profile (id, name, allow_public_message) VALUES (?, ?, ?)"

    override fun toSql(i: UserAndPassword): SQLStatement =
            SQLStatement.Parameterized(insertUser, JsonArray(listOf(i.user.id, i.user.name, i.user.allowPubliMessage)))

    override fun mapResult(i: UserAndPassword, rs: UpdateResult): Try<UserAndPassword> = Try.Success(i)
}

object InsertUserPasswordMapping: UpdateSQLMapping<UserAndPassword, UserAndPassword> {
    val insertUserPassword = "INSERT into user_password (user_id, pswhash) VALUES (?, crypt(?, gen_salt('bf')))"

    override fun toSql(i: UserAndPassword): SQLStatement =
            SQLStatement.Parameterized(insertUserPassword, JsonArray(listOf(i.user.id, i.password)))

    override fun mapResult(i: UserAndPassword, rs: UpdateResult): Try<UserAndPassword> = Try.Success(i)
}

object SelectUserByPassword: QuerySQLMapping<UserIdAndPassword, String> {
    val selectUserPassword = "SELECT user_id FROM user_password WHERE user_id = ? AND pswhash = crypt(?, pswhash)"

    override fun toSql(i: UserIdAndPassword): SQLStatement =
            SQLStatement.Parameterized(selectUserPassword, JsonArray(listOf(i.id, i.password)))

    override fun mapResult(i: UserIdAndPassword, rs: ResultSet): Try<String> =
            rs.rows.map { it.getString(("user_id")) }
                    .firstOrNull()
                    ?.let { Try.Success(it) }
                    ?:Try.Failure<String>(UserError.AuthenticationFailed)
}

object SelectUserIdMapping: QuerySQLMapping<UserNameAndPassword, UserIdAndPassword> {
    val selectUserId = "SELECT id from user_profile WHERE name = ?"

    override fun toSql(i: UserNameAndPassword): SQLStatement = SQLStatement.Parameterized(selectUserId, JsonArray(listOf(i.userName)))


    override fun mapResult(i: UserNameAndPassword, rs: ResultSet): Try<UserIdAndPassword> =
            rs.rows.map { UserIdAndPassword(it.getString("id"), i.password) }
                    .firstOrNull()
                    ?.let { Try.Success(it) }
                    ?:Try.Failure<UserIdAndPassword>(UserError.NoSuchUser(i.userName))
}
