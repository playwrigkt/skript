package dev.yn.playground.auth

import dev.yn.playground.Task
import dev.yn.playground.auth.sql.query.AuthQueries
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.sql.*
import dev.yn.playground.user.models.UserError
import dev.yn.playground.user.sql.UserSQL
import org.funktionale.tries.Try
import java.time.Instant

object AuthTasks {
    fun <T> validateAction() =
            Task.identity<TokenAndInput<T>, ApplicationContext>()
                    .query(AuthQueries.SelectSessionByKey())


}