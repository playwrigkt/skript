package playwrigkt.skript.venue

import playwrigkt.skript.ex.lift
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.user.http.createUserHttpEndpointSkript
import playwrigkt.skript.user.http.getUserHttpEndpointSkript
import playwrigkt.skript.user.http.loginUserHttpEndpointSkript

fun userProduktions(serverVenue: HttpServerVenue, stageManager: ApplicationStageManager): AsyncResult<List<Produktion>> =
    listOf(
            serverVenue.produktion(
                    createUserHttpEndpointSkript,
                    stageManager,
                    HttpServer.Endpoint(
                            "/users",
                            emptyMap(),
                            Http.Method.Post)),
            serverVenue.produktion(
                    loginUserHttpEndpointSkript,
                    stageManager,
                    HttpServer.Endpoint(
                            "/login",
                            emptyMap(),
                            Http.Method.Post)),
            serverVenue.produktion(
                    getUserHttpEndpointSkript,
                    stageManager,
                    HttpServer.Endpoint(
                            "/users/{userId}",
                            mapOf("Authorization" to emptyList()),
                            Http.Method.Get)))
            .lift()
