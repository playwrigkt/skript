package playwrigkt.skript.stage

import playwrigkt.skript.performer.PublishPerformer

interface PublishStage<Message> {
    fun getPublishPerformer(): PublishPerformer<Message>
}