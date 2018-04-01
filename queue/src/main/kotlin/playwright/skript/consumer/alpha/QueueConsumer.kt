package playwright.skript.consumer.alpha

import playwrigkt.skript.consumer.alpha.Venue

data class QueueMessage(val source: String, val body: ByteArray)

typealias QueueVenue = Venue<String, QueueMessage>