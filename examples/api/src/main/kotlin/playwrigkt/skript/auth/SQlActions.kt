package playwrigkt.skript.auth

import playwrigkt.skript.Skript
import playwrigkt.skript.ex.query
import playwrigkt.skript.troupe.ApplicationTroupe

object AuthSkripts {
    fun <T> validateAction() =
            Skript.identity<playwrigkt.skript.auth.TokenAndInput<T>, ApplicationTroupe>()
                    .query(playwrigkt.skript.auth.sql.query.AuthQueries.SelectSessionByKey())


}