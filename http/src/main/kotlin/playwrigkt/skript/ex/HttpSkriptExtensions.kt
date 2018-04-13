package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.http.*
import playwrigkt.skript.troupe.HttpRequestTroupe

fun <I, O, Troupe> Skript<I, O, Troupe>.httpRequest(method: HttpMethod,
                                                    uri: Skript<O, String, Troupe>,
                                                    pathParameters: Skript<O, Map<String, String>, Troupe> = Skript.map { emptyMap() },
                                                    queryParameters: Skript<O, Map<String, String>, Troupe> = Skript.map { emptyMap() },
                                                    headers: Skript<O, Map<String, List<String>>, Troupe> = Skript.map { emptyMap() },
                                                    body: Skript<O, ByteArray, Troupe> = Skript.map { ByteArray(0) }): Skript<I, Http.Client.Request, Troupe> =
        this.andThen(HttpRequestSkript.RequestMapping(
                method, uri, pathParameters, queryParameters, headers, body
        ))

fun <I, Troupe> Skript<I, Http.Client.Request, Troupe>.executeRequest(): Skript<I, Http.Client.Response, Troupe> where Troupe: HttpRequestTroupe =
        this.andThen(HttpRequestSkript())

fun <I, O, Troupe> Skript<I, Http.Client.Response, Troupe>.httpResponse(mappers: List<Pair<IntRange, Skript<Http.Client.Response, O, Troupe>>>): Skript<I, O, Troupe> =
        this.andThen(HttpRequestSkript.ResponseMapping(mappers))

fun <I, O, Troupe> Skript<I, Http.Client.Response, Troupe>.httpResponse(skript: Skript<Http.Client.Response, O, Troupe>): Skript<I, O, Troupe> =
        this.andThen(HttpRequestSkript.ResponseMapping(listOf(Integer.MIN_VALUE..Integer.MAX_VALUE to skript)))

