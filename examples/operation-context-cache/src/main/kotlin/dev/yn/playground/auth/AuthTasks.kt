package dev.yn.playground.auth

import dev.yn.playground.Skript
import dev.yn.playground.auth.context.UserSessionCache
import dev.yn.playground.auth.sql.query.AuthQueries
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.ex.query

object AuthTasks {
    fun <T, R: UserSessionCache> validate(): Skript<T, T, ApplicationContext<R>> =
            Skript.updateContext(
                    Skript.identity<T, ApplicationContext<R>>()
                            .mapWithContext { i, c -> c.cache.getUserSessionKey() }
                            .query(AuthQueries.SelectSessionByKey)
                            .mapWithContext { session, c -> c.cache.setUserSession(session) })

}