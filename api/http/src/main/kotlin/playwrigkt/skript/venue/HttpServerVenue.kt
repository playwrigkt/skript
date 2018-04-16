package playwrigkt.skript.venue

import playwrigkt.skript.http.server.HttpServer

typealias HttpServerVenue = Venue<HttpServer.Endpoint, HttpServer.Request<ByteArray>, HttpServer.Response>