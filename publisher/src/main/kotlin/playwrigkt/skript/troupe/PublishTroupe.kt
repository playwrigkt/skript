package playwrigkt.skript.troupe

import playwrigkt.skript.performer.PublishPerformer
import playwrigkt.skript.result.AsyncResult

interface PublishTroupe<Message> {
    fun getPublishPerformer(): AsyncResult<out PublishPerformer<Message>>
}