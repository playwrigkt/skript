package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.http.*
import playwrigkt.skript.http.client.*
import playwrigkt.skript.troupe.HttpClientTroupe

fun <I, O, Troupe> Skript<I, O, Troupe>.httpRequest(method: Http.Method,
                                                    uri: Skript<O, HttpClient.URI, Troupe>,
                                                    headers: Skript<O, Map<String, List<String>>, Troupe> = Skript.map { emptyMap() },
                                                    body: Skript<O, ByteArray, Troupe> = Skript.map { ByteArray(0) }): Skript<I, HttpClient.Request, Troupe> =
        this.andThen(HttpClientRequestMappingSkript(method, uri, headers, body))

fun <I, Troupe> Skript<I, HttpClient.Request, Troupe>.executeRequest(): Skript<I, HttpClient.Response, Troupe> where Troupe: HttpClientTroupe =
        this.andThen(HttpClientSkript())

fun <I, O, Troupe> Skript<I, HttpClient.Response, Troupe>.httpResponse(mappers: List<Pair<IntRange, Skript<HttpClient.Response, O, Troupe>>>): Skript<I, O, Troupe> =
        this.andThen(HttpClientResponseMappingSkript(mappers))

fun <I, O, Troupe> Skript<I, HttpClient.Response, Troupe>.httpResponse(skript: Skript<HttpClient.Response, O, Troupe>): Skript<I, O, Troupe> =
        this.andThen(HttpClientResponseMappingSkript(listOf(Integer.MIN_VALUE..Integer.MAX_VALUE to skript)))

fun <I, Troupe> uri(useSsl: Skript<I, Boolean, Troupe>,
                    host: Skript<I, String, Troupe>,
                    port: Skript<I, Int?, Troupe>,
                    pathTemplate: Skript<I, String, Troupe>,
                    pathParameters: Skript<I, Map<String, String>, Troupe>,
                    queryParameters: Skript<I, Map<String, List<String>>, Troupe>): Skript<I, HttpClient.URI, Troupe> =
        HttpClientUriMappingSkript(useSsl, host, port, pathTemplate, pathParameters, queryParameters)

fun <I, O, Troupe> Skript<I, O, Troupe>.uri(useSsl: Skript<O, Boolean, Troupe>,
                                            host: Skript<O, String, Troupe>,
                                            port: Skript<O, Int?, Troupe>,
                                            pathTemplate: Skript<O, String, Troupe>,
                                            pathParameters: Skript<O, Map<String, String>, Troupe>,
                                            queryParameters: Skript<O, Map<String, List<String>>, Troupe>): Skript<I, HttpClient.URI, Troupe> =
        this.andThen(HttpClientUriMappingSkript(useSsl, host, port, pathTemplate, pathParameters, queryParameters))
