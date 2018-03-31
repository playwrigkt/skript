package playwrigkt.skript.performer

import playwrigkt.skript.result.AsyncResult

interface PublishPerformer<Message> {
    fun publish(command: Message): AsyncResult<Unit>
}