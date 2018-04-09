package playwrigkt.skript.venue

import playwrigkt.skript.http.HttpEndpoint
import playwrigkt.skript.http.HttpServerRequest
import playwrigkt.skript.http.HttpServerResponse

typealias HttpServerVenue = Venue<HttpEndpoint, HttpServerRequest<ByteArray>, HttpServerResponse>