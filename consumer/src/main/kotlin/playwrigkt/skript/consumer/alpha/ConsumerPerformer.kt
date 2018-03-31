package playwrigkt.skript.consumer.alpha

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.venue.Venue

interface ConsumerStage<Rule, Beginning> {
    fun <Stage> buildPerformer(target: Rule, venue: Venue<Stage>): playwrigkt.skript.consumer.alpha.ConsumerPerformer<Stage, Beginning>
}

interface ConsumerPerformer<Stage, Beginning> {
    fun <Ending> sink(skript: Skript<Beginning, Ending, Stage>): AsyncResult<playwrigkt.skript.consumer.alpha.Sink>
    fun <Ending> stream(skript: Skript<Beginning, Ending, Stage>): AsyncResult<playwrigkt.skript.consumer.alpha.Stream<Ending>>
}
