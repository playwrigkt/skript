package dev.yn.playground.auth.sql

import dev.yn.playground.auth.AuthSession
import dev.yn.playground.auth.SessionAndInput
import dev.yn.playground.auth.TokenAndInput
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.sql.QuerySQLMapping
import dev.yn.playground.sql.SQLStatement
import dev.yn.playground.sql.UnpreparedSQLAction
import dev.yn.playground.user.UserError
import dev.yn.playground.user.UserSQL
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.ResultSet
import org.funktionale.tries.Try
import java.time.Instant

object AuthSQLActions {
    fun <T> validateAction() = UnpreparedSQLAction.query<TokenAndInput<T>, SessionAndInput<T>, ApplicationContextProvider>(SelectSessionByKey())

    class SelectSessionByKey<T>: QuerySQLMapping<TokenAndInput<T>, SessionAndInput<T>> {
        override fun mapResult(i: TokenAndInput<T>, rs: ResultSet): Try<SessionAndInput<T>> =
                rs.rows
                        .firstOrNull()
                        ?.let { Try { SessionAndInput(AuthSession.User(it.getString("user_id"), it.getInstant("expiration")), i.input) } }
                        ?.flatMap {
                            if(it.session.expiration.isBefore(Instant.now())) {
                                Try.Failure<SessionAndInput<T>>(UserError.SessionExpired)
                            } else {
                                Try.Success(it)
                            } }
                        ?: Try.Failure<SessionAndInput<T>>(UserError.AuthenticationFailed)


        override fun toSql(i: TokenAndInput<T>): SQLStatement =
                SQLStatement.Parameterized(UserSQL.selectSessionByKey, JsonArray(listOf(i.token)))
    }
}