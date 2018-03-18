package playwright.skript.auth

import playwright.skript.Skript
import playwright.skript.auth.props.UserSessionProps
import playwright.skript.auth.sql.query.AuthQueries
import playwright.skript.common.ApplicationStage
import playwright.skript.ex.query

object AuthSkripts {
    fun <T, R: UserSessionProps> validate(): Skript<T, T, ApplicationStage<R>> =
            Skript.updateStage(
                    Skript.identity<T, ApplicationStage<R>>()
                            .mapWithStage { _, stage -> stage.getStageProps().getUserSessionKey() }
                            .query(AuthQueries.SelectSessionByKey)
                            .mapWithStage { session, stage -> stage.getStageProps().setUserSession(session) })

}