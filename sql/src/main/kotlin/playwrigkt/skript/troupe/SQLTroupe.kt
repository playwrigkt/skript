package playwrigkt.skript.troupe

import playwrigkt.skript.performer.SQLPerformer

interface SQLTroupe {
    fun getSQLPerformer(): SQLPerformer
}