package playwright.skript.consumer.alpha

import playwrigkt.skript.Skript
import playwrigkt.skript.consumer.alpha.ConsumerProduction
import playwrigkt.skript.consumer.alpha.ConsumerTroupe
import playwrigkt.skript.consumer.alpha.Sink
import playwrigkt.skript.consumer.alpha.Stream
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.venue.Venue

data class QueueMessage(val source: String, val body: ByteArray)

interface QueueConsumerTroupe: ConsumerTroupe<String, QueueMessage> {
    override fun <Stage> production(target: String, venue: Venue<Stage>): QueueConsumerProduction<Stage>
}

interface QueueConsumerProduction<Stage>: ConsumerProduction<Stage, QueueMessage> {
    override fun <Ending> sink(skript: Skript<QueueMessage, Ending, Stage>): AsyncResult<Sink>
    override fun <Ending> stream(skript: Skript<QueueMessage, Ending, Stage>): AsyncResult<Stream<Ending>>
}