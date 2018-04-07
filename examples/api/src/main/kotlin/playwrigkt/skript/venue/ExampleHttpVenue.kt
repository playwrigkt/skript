package playwrigkt.skript.venue

import playwrigkt.skript.http.HttpEndpoint
import playwrigkt.skript.http.HttpMethod
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

fun applyHandlers(venue: HttpVenue, stageManager: ApplicationStageManager): AsyncResult<List<Produktion>> =
    listOf(
            venue.produktion(
                    createUserHttpEndpointSkript,
                    stageManager,
                    HttpEndpoint(
                            "/users",
                            emptyMap(),
                            HttpMethod.Put)),
            venue.produktion(
                    loginUserHttpEndpointSkript,
                    stageManager,
                    HttpEndpoint(
                            "/login",
                            emptyMap(),
                            HttpMethod.Post)),
            venue.produktion(
                    getUserHttpEndpointSkript,
                    stageManager,
                    HttpEndpoint(
                            "/users/{userId}",
                            mapOf("Authorization" to emptyList()),
                            HttpMethod.Get)))
            .lift()
