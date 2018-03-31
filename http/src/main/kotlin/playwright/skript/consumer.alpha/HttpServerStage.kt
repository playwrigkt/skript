package playwright.skript.consumer.alpha

import playwrigkt.skript.consumer.alpha.ConsumerTroupe
import playwrigkt.skript.venue.Venue

interface HttpTroupe: ConsumerTroupe<HttpEndpoint, HttpRequest> {
    fun <STAGE> listen(endpoint: HttpEndpoint, venue: Venue<STAGE>): HttpConsumer<STAGE>
}