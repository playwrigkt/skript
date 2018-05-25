package playwrigkt.skript.auth.sql.query

import arrow.core.Try
import arrow.core.recoverWith
import playwrigkt.skript.sql.SqlCommand
import playwrigkt.skript.sql.SqlMapping
import playwrigkt.skript.sql.SqlResult
import playwrigkt.skript.sql.SqlStatement
import playwrigkt.skript.user.models.UserError
import playwrigkt.skript.user.sql.UserSql
import java.time.Instant

object AuthQueries {
    class SelectSessionByKey<T>: SqlMapping<playwrigkt.skript.auth.TokenAndInput<T>, playwrigkt.skript.auth.SessionAndInput<T>, SqlCommand.Query, SqlResult.Query> {
        override fun mapResult(i: playwrigkt.skript.auth.TokenAndInput<T>, rs: SqlResult.Query): Try<playwrigkt.skript.auth.SessionAndInput<T>> =
                Try { rs.result.next() }
                        .map {
                            playwrigkt.skript.auth.SessionAndInput(
                                    playwrigkt.skript.auth.AuthSession.User(
                                            it.getString("user_id"),
                                            it.getInstant("expiration")),
                                    i.input)

                        }
                        .recoverWith { Try.Failure(UserError.AuthenticationFailed) }
                        .flatMap {
                            if(it.session.expiration.isBefore(Instant.now())) {
                                Try.Failure<playwrigkt.skript.auth.SessionAndInput<T>>(UserError.SessionExpired)
                            } else {
                                Try.Success(it)
                            } }



        override fun toSql(i: playwrigkt.skript.auth.TokenAndInput<T>): SqlCommand.Query =
                SqlCommand.Query(SqlStatement.Parameterized(UserSql.selectSessionByKey, listOf(i.token)))
    }
}