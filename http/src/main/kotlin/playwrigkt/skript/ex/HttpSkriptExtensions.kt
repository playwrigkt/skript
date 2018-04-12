package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.http.HttpClientRequest
import playwrigkt.skript.http.HttpClientResponse
import playwrigkt.skript.http.HttpMethod
import playwrigkt.skript.http.HttpRequestSkript
import playwrigkt.skript.troupe.HttpRequestTroupe

fun <I, O, Troupe> Skript<I, O, Troupe>.httpRequest(method: HttpMethod,
                                                    uri: Skript<O, String, Troupe>,
                                                    pathParameters: Skript<O, Map<String, String>, Troupe> = Skript.map { emptyMap() },
                                                    queryParameters: Skript<O, Map<String, String>, Troupe> = Skript.map { emptyMap() },
                                                    headers: Skript<O, Map<String, List<String>>, Troupe> = Skript.map { emptyMap() },
                                                    body: Skript<O, ByteArray, Troupe> = Skript.map { ByteArray(0) }): Skript<I, HttpClientRequest, Troupe> =
        this.andThen(HttpRequestSkript.RequestMapping(
                method, uri, pathParameters, queryParameters, headers, body
        ))

fun <I, Troupe> Skript<I, HttpClientRequest, Troupe>.executeRequest(): Skript<I, HttpClientResponse, Troupe> where Troupe: HttpRequestTroupe =
        this.andThen(HttpRequestSkript())

fun <I, O, Troupe> Skript<I, HttpClientResponse, Troupe>.httpResponse(mappers: List<Pair<IntRange, Skript<HttpClientResponse, O, Troupe>>>): Skript<I, O, Troupe> =
        this.andThen(HttpRequestSkript.ResponseMapping(mappers))

fun <I, O, Troupe> Skript<I, HttpClientResponse, Troupe>.httpResponse(skript: Skript<HttpClientResponse, O, Troupe>): Skript<I, O, Troupe> =
        this.andThen(HttpRequestSkript.ResponseMapping(listOf(Integer.MIN_VALUE..Integer.MAX_VALUE to skript)))

