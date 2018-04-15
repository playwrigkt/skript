package playwrigkt.skript.auth.sql.query

import org.funktionale.tries.Try
import playwrigkt.skript.sql.SQLCommand
import playwrigkt.skript.sql.SQLMapping
import playwrigkt.skript.sql.SQLResult
import playwrigkt.skript.sql.SQLStatement
import playwrigkt.skript.user.models.UserError
import playwrigkt.skript.user.sql.UserSQL
import java.time.Instant

object AuthQueries {
    class SelectSessionByKey<T>: SQLMapping<playwrigkt.skript.auth.TokenAndInput<T>, playwrigkt.skript.auth.SessionAndInput<T>, SQLCommand.Query, SQLResult.Query> {
        override fun mapResult(i: playwrigkt.skript.auth.TokenAndInput<T>, rs: SQLResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<T>> =
                Try { rs.result.next() }
                        .map {
                            playwrigkt.skript.auth.SessionAndInput(
                                    playwrigkt.skript.auth.AuthSession.User(
                                            it.getString("user_id"),
                                            it.getInstant("expiration")),
                                    i.input)

                        }
                        .rescue { Try.Failure(UserError.AuthenticationFailed) }
                        .flatMap {
                            if(it.session.expiration.isBefore(Instant.now())) {
                                Try.Failure<playwrigkt.skript.auth.SessionAndInput<T>>(UserError.SessionExpired)
                            } else {
                                Try.Success(it)
                            } }



        override fun toSql(i: playwrigkt.skript.auth.TokenAndInput<T>): SQLCommand.Query =
                SQLCommand.Query(SQLStatement.Parameterized(UserSQL.selectSessionByKey, listOf(i.token)))
    }
}