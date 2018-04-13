package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.HttpClientSkript
import playwrigkt.skript.troupe.HttpClientTroupe

fun <I, O, Troupe> Skript<I, O, Troupe>.httpRequest(method: Http.Method,
                                                    uri: Skript<O, String, Troupe>,
                                                    pathParameters: Skript<O, Map<String, String>, Troupe> = Skript.map { emptyMap() },
                                                    queryParameters: Skript<O, Map<String, String>, Troupe> = Skript.map { emptyMap() },
                                                    headers: Skript<O, Map<String, List<String>>, Troupe> = Skript.map { emptyMap() },
                                                    body: Skript<O, ByteArray, Troupe> = Skript.map { ByteArray(0) }): Skript<I, Http.Client.Request, Troupe> =
        this.andThen(HttpClientSkript.RequestMappingSkript(
                method, uri, pathParameters, queryParameters, headers, body
        ))

fun <I, Troupe> Skript<I, Http.Client.Request, Troupe>.executeRequest(): Skript<I, Http.Client.Response, Troupe> where Troupe: HttpClientTroupe =
        this.andThen(HttpClientSkript())

fun <I, O, Troupe> Skript<I, Http.Client.Response, Troupe>.httpResponse(mappers: List<Pair<IntRange, Skript<Http.Client.Response, O, Troupe>>>): Skript<I, O, Troupe> =
        this.andThen(HttpClientSkript.ResponseMappingSkript(mappers))

fun <I, O, Troupe> Skript<I, Http.Client.Response, Troupe>.httpResponse(skript: Skript<Http.Client.Response, O, Troupe>): Skript<I, O, Troupe> =
        this.andThen(HttpClientSkript.ResponseMappingSkript(listOf(Integer.MIN_VALUE..Integer.MAX_VALUE to skript)))

fun <I, Troupe> uri(useSsl: Skript<I, Boolean, Troupe>,
                    host: Skript<I, String, Troupe>,
                    port: Skript<I, Int?, Troupe>,
                    pathTemplate: Skript<I, String, Troupe>): Skript<I, String, Troupe> =
        HttpClientSkript.UriMappingSkript(useSsl, host, port, pathTemplate)

fun <I, O, Troupe> Skript<I, O, Troupe>.uri(useSsl: Skript<O, Boolean, Troupe>,
                    host: Skript<O, String, Troupe>,
                    port: Skript<O, Int?, Troupe>,
                    pathTemplate: Skript<O, String, Troupe>): Skript<I, String, Troupe> =
        this.andThen(HttpClientSkript.UriMappingSkript(useSsl, host, port, pathTemplate))

