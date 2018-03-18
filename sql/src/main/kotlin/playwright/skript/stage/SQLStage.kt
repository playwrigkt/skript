package playwright.skript.stage

import playwright.skript.performer.SQLPerformer

interface SQLStage {
    fun getSQLPerformer(): SQLPerformer
}