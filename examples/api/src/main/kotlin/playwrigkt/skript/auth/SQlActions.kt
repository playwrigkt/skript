package playwrigkt.skript.auth

import playwrigkt.skript.Skript
import playwrigkt.skript.common.ApplicationTroupe
import playwrigkt.skript.ex.query

object AuthSkripts {
    fun <T> validateAction() =
            Skript.identity<playwrigkt.skript.auth.TokenAndInput<T>, ApplicationTroupe>()
                    .query(playwrigkt.skript.auth.sql.query.AuthQueries.SelectSessionByKey())


}