package playwrigkt.skript.venue

import playwrigkt.skript.http.Http
import playwrigkt.skript.http.HttpEndpoint

typealias HttpServerVenue = Venue<HttpEndpoint, Http.Server.Request<ByteArray>, Http.Server.Response>