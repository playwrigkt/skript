package playwrigkt.skript.stage

import playwrigkt.skript.performer.PublishPerformer

interface PublishStage {
    fun getPublishPerformer(): PublishPerformer
}