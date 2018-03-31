package playwright.skript.consumer.alpha

import playwrigkt.skript.consumer.alpha.ConsumerProduction
import playwrigkt.skript.consumer.alpha.ConsumerTroupe
import playwrigkt.skript.venue.Venue

data class QueueMessage(val source: String, val body: ByteArray)

interface QueueConsumerTroupe: ConsumerTroupe<String, QueueMessage> {
    override fun <Stage> production(target: String, venue: Venue<Stage>): QueueConsumerProduction<Stage>
}

typealias QueueConsumerProduction<Stage> = ConsumerProduction<Stage, QueueMessage>