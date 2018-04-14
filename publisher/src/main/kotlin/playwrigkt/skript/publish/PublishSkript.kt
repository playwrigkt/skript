package playwrigkt.skript.publish

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.PublishTroupe

sealed class PublishSkript<Message>: Skript<Message, Unit, PublishTroupe<Message>> {

    companion object {
        fun<Message>publish(): PublishSkript<Message> {
            return Publish()
        }
    }
    class Publish<Message>(): PublishSkript<Message>() {
        override fun run(i: Message, troupe: PublishTroupe<Message>): AsyncResult<Unit> {
            return troupe.getPublishPerformer()
                    .flatMap { publishPerformer -> publishPerformer.publish(i) }
        }
    }
}