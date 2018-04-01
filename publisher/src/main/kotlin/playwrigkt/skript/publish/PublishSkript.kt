package playwrigkt.skript.publish

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.PublishTroupe

sealed class PublishSkript<I, Message>: Skript<I, I, PublishTroupe<Message>> {

    companion object {
        fun<I, Message>publish(mapping: (I) -> Message): PublishSkript<I, Message> {
            return Publish(mapping)
        }
    }
    data class Publish<I, Message>(val mapping: (I) -> Message): PublishSkript<I, Message>() {
        override fun run(i: I, troupe: PublishTroupe<Message>): AsyncResult<I> {
            return troupe.getPublishPerformer()
                    .publish(mapping(i))
                    .map { i }
        }
    }
}