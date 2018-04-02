package playwrigkt.skript.auth

import playwrigkt.skript.Skript
import playwrigkt.skript.auth.props.UserSessionTroupeProps
import playwrigkt.skript.auth.sql.query.AuthQueries
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.ex.query

object AuthSkripts {
    fun <T, R: UserSessionTroupeProps> validate(): Skript<T, T, ApplicationTroupe<R>> =
            Skript.updateTroupe(
                    Skript.identity<T, ApplicationTroupe<R>>()
                            .mapWithTroupe { _, stage -> stage.getTroupeProps().getUserSessionKey() }
                            .query(AuthQueries.SelectSessionByKey)
                            .mapWithTroupe { session, stage -> stage.getTroupeProps().setUserSession(session) })

}