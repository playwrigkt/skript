package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.client.*
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.http.server.HttpServerResponseMappingSkript
import playwrigkt.skript.troupe.HttpClientTroupe

/**
 * Chain a skript that converts to an HttpClient.Request
 */
fun <I, O, Troupe> Skript<I, O, Troupe>.httpClientRequest(method: Http.Method,
                                                          uri: Skript<O, HttpClient.URI, Troupe>,
                                                          headers: Skript<O, Map<String, List<String>>, Troupe> = Skript.map { emptyMap() },
                                                          body: Skript<O, ByteArray, Troupe> = Skript.map { ByteArray(0) }): Skript<I, HttpClient.Request, Troupe> =
        this.andThen(HttpClientRequestMappingSkript(method, uri, headers, body))

/**
 * Chain a skript that executes an HttpClient.Request
 */
fun <I, Troupe> Skript<I, HttpClient.Request, Troupe>.executeRequest(): Skript<I, HttpClient.Response, Troupe> where Troupe: HttpClientTroupe =
        this.andThen(HttpClientSkript())

/**
 * Chain a skript that parses and HttpClient.Response
 */
fun <I, O, Troupe> Skript<I, HttpClient.Response, Troupe>.httpClientResponse(skript: Skript<HttpClient.Response, O, Troupe>): Skript<I, O, Troupe> =
        this.andThen(skript)

/**
 * Chains a skript that maps to an HttpServer.Response
 */
fun <I, O, Troupe> Skript<I, O, Troupe>.httpServerResponse(
        status: Skript<O, Http.Status, Troupe>,
        headers: Skript<O, Map<String, List<String>>, Troupe>,
        body: Skript<O, ByteArray, Troupe>,
        error: Skript<Throwable, HttpServer.Response, Troupe>): Skript<I, HttpServer.Response, Troupe> =
        this.andThen(HttpServerResponseMappingSkript(status, headers, body, error))

/**
 * Create a skript that creates an HttpClient.URI
 */
fun <I, Troupe> uri(useSsl: Skript<I, Boolean, Troupe>,
                    host: Skript<I, String, Troupe>,
                    port: Skript<I, Int?, Troupe>,
                    pathTemplate: Skript<I, String, Troupe>,
                    pathParameters: Skript<I, Map<String, String>, Troupe>,
                    queryParameters: Skript<I, Map<String, List<String>>, Troupe>): Skript<I, HttpClient.URI, Troupe> =
        HttpClientUriMappingSkript(useSsl, host, port, pathTemplate, pathParameters, queryParameters)

/**
 * Chain a skript that creates an HttpClient.URI
 */
fun <I, O, Troupe> Skript<I, O, Troupe>.uri(useSsl: Skript<O, Boolean, Troupe>,
                                            host: Skript<O, String, Troupe>,
                                            port: Skript<O, Int?, Troupe>,
                                            pathTemplate: Skript<O, String, Troupe>,
                                            pathParameters: Skript<O, Map<String, String>, Troupe>,
                                            queryParameters: Skript<O, Map<String, List<String>>, Troupe>): Skript<I, HttpClient.URI, Troupe> =
        this.andThen(HttpClientUriMappingSkript(useSsl, host, port, pathTemplate, pathParameters, queryParameters))
