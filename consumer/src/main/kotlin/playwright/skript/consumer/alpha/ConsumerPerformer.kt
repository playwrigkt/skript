package playwright.skript.consumer.alpha

import playwright.skript.Skript
import playwright.skript.result.AsyncResult
import playwright.skript.venue.Venue

interface ConsumerStage {
    fun <Stage> buildPerformer(target: String, venue: Venue<Stage>): ConsumerPerformer<Stage>
}

interface ConsumerPerformer<Stage> {
    fun <O> sink(skript: Skript<ConsumedMessage, O, Stage>): AsyncResult<Sink>
    fun <O> stream(skript: Skript<ConsumedMessage, O, Stage>): AsyncResult<Stream<O>>
}
