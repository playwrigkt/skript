package playwright.skript.consumer.alpha

import playwrigkt.skript.consumer.alpha.ConsumerStage
import playwrigkt.skript.venue.Venue

interface HttpStage: ConsumerStage<HttpEndpoint, HttpRequest> {
    fun <STAGE> listen(endpoint: HttpEndpoint, venue: Venue<STAGE>): HttpConsumer<STAGE>
}