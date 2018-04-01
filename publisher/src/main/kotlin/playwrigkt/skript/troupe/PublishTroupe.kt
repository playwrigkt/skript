package playwrigkt.skript.troupe

import playwrigkt.skript.performer.PublishPerformer

interface PublishTroupe<Message> {
    fun getPublishPerformer(): PublishPerformer<Message>
}