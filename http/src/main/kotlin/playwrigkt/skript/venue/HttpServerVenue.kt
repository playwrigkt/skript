package playwrigkt.skript.venue

import playwrigkt.skript.http.Http

typealias HttpServerVenue = Venue<Http.Server.Endpoint, Http.Server.Request<ByteArray>, Http.Server.Response>