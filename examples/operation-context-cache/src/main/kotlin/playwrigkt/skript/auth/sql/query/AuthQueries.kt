package playwrigkt.skript.auth.sql.query

import org.funktionale.tries.Try
import playwrigkt.skript.auth.AuthSession
import playwrigkt.skript.sql.SQLCommand
import playwrigkt.skript.sql.SQLMapping
import playwrigkt.skript.sql.SQLResult
import playwrigkt.skript.sql.SQLStatement
import playwrigkt.skript.user.models.UserError
import playwrigkt.skript.user.sql.UserSQL
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