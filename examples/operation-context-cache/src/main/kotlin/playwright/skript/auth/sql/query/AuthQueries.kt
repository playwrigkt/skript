package playwright.skript.auth.sql.query

import org.funktionale.tries.Try
import playwright.skript.auth.AuthSession
import playwright.skript.sql.SQLCommand
import playwright.skript.sql.SQLMapping
import playwright.skript.sql.SQLResult
import playwright.skript.sql.SQLStatement
import playwright.skript.user.models.UserError
import playwright.skript.user.sql.UserSQL
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