package playwright.skript.consumer.alpha

import playwright.skript.Skript
import playwright.skript.result.AsyncResult
import playwright.skript.venue.Venue

interface ConsumerStage {
    fun <C> buildPerformer(target: String, venue: Venue<C>): playwright.skript.consumer.alpha.ConsumerPerformer<C>
}

interface ConsumerPerformer<C> {
    fun <O> sink(skript: Skript<playwright.skript.consumer.alpha.ConsumedMessage, O, C>): AsyncResult<playwright.skript.consumer.alpha.Sink>
    fun <O> stream(skript: Skript<playwright.skript.consumer.alpha.ConsumedMessage, O, C>): AsyncResult<playwright.skript.consumer.alpha.Stream<O>>
}
