package playwright.skript.auth.sql.query

import org.funktionale.tries.Try
import playwright.skript.sql.SQLCommand
import playwright.skript.sql.SQLMapping
import playwright.skript.sql.SQLResult
import playwright.skript.sql.SQLStatement
import playwright.skript.user.models.UserError
import playwright.skript.user.sql.UserSQL
import java.time.Instant

object AuthQueries {
    class SelectSessionByKey<T>: SQLMapping<playwright.skript.auth.TokenAndInput<T>, playwright.skript.auth.SessionAndInput<T>, SQLCommand.Query, SQLResult.Query> {
        override fun mapResult(i: playwright.skript.auth.TokenAndInput<T>, rs: SQLResult.Query): Try<playwright.skript.auth.SessionAndInput<T>> =
                Try { rs.result.next() }
                        .map {
                            playwright.skript.auth.SessionAndInput(
                                    playwright.skript.auth.AuthSession.User(
                                            it.getString("user_id"),
                                            it.getInstant("expiration")),
                                    i.input)

                        }
                        .rescue { Try.Failure(UserError.AuthenticationFailed) }
                        .flatMap {
                            if(it.session.expiration.isBefore(Instant.now())) {
                                Try.Failure<playwright.skript.auth.SessionAndInput<T>>(UserError.SessionExpired)
                            } else {
                                Try.Success(it)
                            } }



        override fun toSql(i: playwright.skript.auth.TokenAndInput<T>): SQLCommand.Query =
                SQLCommand.Query(SQLStatement.Parameterized(UserSQL.selectSessionByKey, listOf(i.token)))
    }
}