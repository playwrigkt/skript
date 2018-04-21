package playwrigkt.skript.user

import playwrigkt.skript.ex.lift
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.user.http.createUserHttpEndpointSkript
import playwrigkt.skript.user.http.getUserHttpEndpointSkript
import playwrigkt.skript.user.http.loginUserHttpEndpointSkript
import playwrigkt.skript.venue.HttpServerVenue

fun userHttpProduktions(httpServerVenue: HttpServerVenue, stageManager: ApplicationStageManager) =
        listOf(createUserHttpProduktion(httpServerVenue, stageManager),
                loginuserHttpProduktion(httpServerVenue, stageManager),
                getUserHttpProduktion(httpServerVenue, stageManager))
                .lift()

fun createUserHttpProduktion(httpServerVenue: HttpServerVenue, stageManager: ApplicationStageManager) =
                httpServerVenue.produktion(
                        createUserHttpEndpointSkript,
                        stageManager,
                        HttpServer.Endpoint(
                                "/users",
                                emptyMap(),
                                Http.Method.Post))
fun loginuserHttpProduktion(httpServerVenue: HttpServerVenue, stageManager: ApplicationStageManager) =
                httpServerVenue.produktion(
                        loginUserHttpEndpointSkript,
                        stageManager,
                        HttpServer.Endpoint(
                                "/login",
                                emptyMap(),
                                Http.Method.Post))
fun getUserHttpProduktion(httpServerVenue: HttpServerVenue, stageManager: ApplicationStageManager) =
                httpServerVenue.produktion(
                        getUserHttpEndpointSkript,
                        stageManager,
                        HttpServer.Endpoint(
                                "/users/{userId}",
                                mapOf("Authorization" to emptyList()),
                                Http.Method.Get))