package playwrigkt.skript.stage

import playwrigkt.skript.performer.PublishPerformer

interface PublishCast {
    fun getPublishPerformer(): PublishPerformer
}