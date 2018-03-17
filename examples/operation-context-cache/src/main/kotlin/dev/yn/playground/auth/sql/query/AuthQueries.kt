package dev.yn.playground.auth.sql.query

import dev.yn.playground.auth.AuthSession
import dev.yn.playground.sql.SQLCommand
import dev.yn.playground.sql.SQLMapping
import dev.yn.playground.sql.SQLResult
import dev.yn.playground.sql.SQLStatement
import dev.yn.playground.user.models.UserError
import dev.yn.playground.user.sql.UserSQL
import org.funktionale.tries.Try
import java.time.Instant

object AuthQueries {
    object SelectSessionByKey: SQLMapping<String, AuthSession, SQLCommand.Query, SQLResult.Query> {
        override fun mapResult(i: String, rs: SQLResult.Query): Try<AuthSession> =
                Try { rs.result.next() }
                        .map {
                            AuthSession.User(
                                    it.getString("user_id"),
                                    it.getInstant("expiration"))

                        }
                        .rescue { Try.Failure(UserError.AuthenticationFailed) }
                        .flatMap<AuthSession> {
                            if(it.expiration.isBefore(Instant.now())) {
                                Try.Failure(UserError.SessionExpired)
                            } else {
                                Try.Success(it)
                            } }



        override fun toSql(i: String): SQLCommand.Query =
                SQLCommand.Query(SQLStatement.Parameterized(UserSQL.selectSessionByKey, listOf(i)))
    }
}