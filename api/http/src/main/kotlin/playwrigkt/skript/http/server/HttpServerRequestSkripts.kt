package playwrigkt.skript.http.server

import arrow.core.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.http.HttpError

fun <Troupe> getHeader(name: String): Skript<HttpServer.Request<ByteArray>, String, Troupe> = Skript.identity<HttpServer.Request<ByteArray>, Troupe>()
        .mapTry {
            it.headers.get(name)
                    ?.firstOrNull()
                    ?.let { Try.Success(it) }
                    ?: Try.Failure<String>(HttpError.MissingInputs(listOf(HttpError.HttpInput.header(name))))
        }

fun <Troupe> getHeaders(name: String): Skript<HttpServer.Request<ByteArray>, List<String>, Troupe> =
        Skript.identity<HttpServer.Request<ByteArray>, Troupe>()
                .map { it.headers.get(name)?: emptyList() }

fun <Troupe> getPathParameter(name: String): Skript<HttpServer.Request<*>, String, Troupe> = Skript
        .identity<HttpServer.Request<*>, Troupe>()
        .mapTry {
    it.pathParameters.get(name)
            ?.let { Try.Success(it) }
            ?: Try.Failure<String>(HttpError.MissingInputs(listOf(HttpError.HttpInput.path(name))))
}

fun <Troupe> getQueryParameter(name: String): Skript<HttpServer.Request<*>, List<String>, Troupe> = Skript
        .identity<HttpServer.Request<*>, Troupe>()
        .mapTry {
            it.queryParameters.get(name)
                    ?.let { Try.Success(it) }
                    ?: Try.Failure<List<String>>(HttpError.MissingInputs(listOf(HttpError.HttpInput.path(name))))
        }