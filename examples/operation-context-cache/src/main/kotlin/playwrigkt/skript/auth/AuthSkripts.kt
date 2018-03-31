package playwrigkt.skript.auth

import playwrigkt.skript.Skript
import playwrigkt.skript.auth.props.UserSessionStageProps
import playwrigkt.skript.auth.sql.query.AuthQueries
import playwrigkt.skript.common.ApplicationStage
import playwrigkt.skript.ex.query

object AuthSkripts {
    fun <T, R: UserSessionStageProps> validate(): Skript<T, T, ApplicationStage<R>> =
            Skript.updateStage(
                    Skript.identity<T, ApplicationStage<R>>()
                            .mapWithStage { _, stage -> stage.getStageProps().getUserSessionKey() }
                            .query(AuthQueries.SelectSessionByKey)
                            .mapWithStage { session, stage -> stage.getStageProps().setUserSession(session) })

}