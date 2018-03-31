package playwrigkt.skript.stage

import playwrigkt.skript.performer.SerializePerformer

interface SerializeStage {
    fun getSerializePerformer(): SerializePerformer
}