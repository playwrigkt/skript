package playwright.skript.auth

import playwright.skript.Skript
import playwright.skript.common.ApplicationStage
import playwright.skript.ex.query

object AuthSkripts {
    fun <T> validateAction() =
            Skript.identity<playwright.skript.auth.TokenAndInput<T>, ApplicationStage>()
                    .query(playwright.skript.auth.sql.query.AuthQueries.SelectSessionByKey())


}