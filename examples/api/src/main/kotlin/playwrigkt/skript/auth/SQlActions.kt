package playwrigkt.skript.auth

import playwrigkt.skript.Skript
import playwrigkt.skript.common.ApplicationStage
import playwrigkt.skript.ex.query

object AuthSkripts {
    fun <T> validateAction() =
            Skript.identity<playwrigkt.skript.auth.TokenAndInput<T>, ApplicationStage>()
                    .query(playwrigkt.skript.auth.sql.query.AuthQueries.SelectSessionByKey())


}