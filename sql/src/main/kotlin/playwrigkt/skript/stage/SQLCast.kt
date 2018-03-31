package playwrigkt.skript.stage

import playwrigkt.skript.performer.SQLPerformer

interface SQLCast {
    fun getSQLPerformer(): SQLPerformer
}