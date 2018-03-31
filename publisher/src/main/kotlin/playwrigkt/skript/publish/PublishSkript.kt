package playwrigkt.skript.publish

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stage.PublishStage

sealed class PublishSkript<I, Message>: Skript<I, I, PublishStage<Message>> {

    companion object {
        fun<I, Message>publish(mapping: (I) -> Message): PublishSkript<I, Message> {
            return Publish(mapping)
        }
    }
    data class Publish<I, Message>(val mapping: (I) -> Message): PublishSkript<I, Message>() {
        override fun run(i: I, stage: PublishStage<Message>): AsyncResult<I> {
            return stage.getPublishPerformer()
                    .publish(mapping(i))
                    .map { i }
        }
    }
}