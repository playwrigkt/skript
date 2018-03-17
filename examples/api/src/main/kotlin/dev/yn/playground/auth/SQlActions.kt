package dev.yn.playground.auth

import dev.yn.playground.Skript
import dev.yn.playground.auth.sql.query.AuthQueries
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.sql.*

object AuthTasks {
    fun <T> validateAction() =
            Skript.identity<TokenAndInput<T>, ApplicationContext>()
                    .query(AuthQueries.SelectSessionByKey())


}