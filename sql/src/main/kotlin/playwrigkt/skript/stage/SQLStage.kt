package playwrigkt.skript.stage

import playwrigkt.skript.performer.SQLPerformer

interface SQLStage {
    fun getSQLPerformer(): SQLPerformer
}