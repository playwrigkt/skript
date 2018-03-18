package playwright.skript.stage

import playwright.skript.performer.SQLPerformer

interface SQLStage<C: SQLPerformer> {
    fun getSQLPerformer(): C
}