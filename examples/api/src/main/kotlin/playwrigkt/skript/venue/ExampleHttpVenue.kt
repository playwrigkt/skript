package playwrigkt.skript.venue

import playwrigkt.skript.http.Http
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.user.http.createUserHttpEndpointSkript
import playwrigkt.skript.user.http.getUserHttpEndpointSkript
import playwrigkt.skript.user.http.loginUserHttpEndpointSkript

fun <T> List<AsyncResult<out T>>.lift(): AsyncResult<List<T>> {
    return this
            .fold(AsyncResult.succeeded(emptyList<T>()))
            { results: AsyncResult<List<T>>, next: AsyncResult<out T> ->
                next.flatMap { t ->
                    results.map { it.plus(t) }
                }
            }
}

fun userProduktions(serverVenue: HttpServerVenue, stageManager: ApplicationStageManager): AsyncResult<List<Produktion>> =
    listOf(
            serverVenue.produktion(
                    createUserHttpEndpointSkript,
                    stageManager,
                    Http.Server.Endpoint(
                            "/users",
                            emptyMap(),
                            Http.Method.Post)),
            serverVenue.produktion(
                    loginUserHttpEndpointSkript,
                    stageManager,
                    Http.Server.Endpoint(
                            "/login",
                            emptyMap(),
                            Http.Method.Post)),
            serverVenue.produktion(
                    getUserHttpEndpointSkript,
                    stageManager,
                    Http.Server.Endpoint(
                            "/users/{userId}",
                            mapOf("Authorization" to emptyList()),
                            Http.Method.Get)))
            .lift()
