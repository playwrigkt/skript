package playwright.skript.auth

import playwright.skript.Skript
import playwright.skript.auth.props.UserSessionProps
import playwright.skript.auth.sql.query.AuthQueries
import playwright.skript.common.ApplicationStage
import playwright.skript.ex.query

object AuthSkripts {
    fun <T, R: UserSessionProps> validate(): Skript<T, T, ApplicationStage<R>> =
            Skript.updateContext(
                    Skript.identity<T, ApplicationStage<R>>()
                            .mapWithContext { i, c -> c.getStageProps().getUserSessionKey() }
                            .query(AuthQueries.SelectSessionByKey)
                            .mapWithContext { session, c -> c.getStageProps().setUserSession(session) })

}