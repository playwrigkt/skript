package playwrigkt.skript.publish

import playwrigkt.skript.Skript
import playwrigkt.skript.performer.PublishCommand
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stage.PublishStage

sealed class PublishSkript<I, O>: Skript<I, O, PublishStage> {

    companion object {
        fun<I>publish(mapping: (I) -> PublishCommand.Publish): PublishSkript<I, I> {
            return Publish(mapping)
        }
    }
    data class Publish<I>(val mapping: (I) -> PublishCommand.Publish): PublishSkript<I, I>() {
        override fun run(i: I, stage: PublishStage): AsyncResult<I> {
            return stage.getPublishPerformer()
                    .publish(mapping(i))
                    .map { i }
        }
    }
}