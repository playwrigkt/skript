package playwright.skript.stage

import playwright.skript.performer.SQLPerformer

interface SQLCast {
    fun getSQLPerformer(): SQLPerformer
}