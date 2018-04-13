package playwrigkt.skript.http

import org.funktionale.option.Option
import org.funktionale.option.getOrElse
import org.funktionale.option.orElse
import org.funktionale.option.toOption
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.HttpRequestTroupe
import playwrigkt.skript.troupe.SerializeTroupe

class HttpRequestSkript: Skript<Http.Client.Request, Http.Client.Response, HttpRequestTroupe> {
    companion object {

        fun <I, O, Troupe> serialized(toRequest: RequestMapping<I, Troupe>,
                                      fromResponse: ResponseMapping<O, Troupe>): Skript<I, O, Troupe> where Troupe: HttpRequestTroupe, Troupe : SerializeTroupe =
            Skript.identity<I, Troupe>()
                    .andThen(toRequest)
                    .andThen(HttpRequestSkript())
                    .andThen(fromResponse)
    }
    override fun run(i: Http.Client.Request, troupe: HttpRequestTroupe): AsyncResult<Http.Client.Response> =
        troupe.getHttpRequestPerformer().flatMap { it.perform(i) }


    data class RequestMapping<I, Troupe>(val method: Http.Method,
                                         val uri: Skript<I, String, Troupe>,
                                         val pathParameters: Skript<I, Map<String, String>, Troupe> = Skript.map { emptyMap() },
                                         val queryParameters: Skript<I, Map<String, String>, Troupe> = Skript.map { emptyMap() },
                                         val headers: Skript<I, Map<String, List<String>>, Troupe> = Skript.map { emptyMap() },
                                         val body:Skript<I, ByteArray, Troupe> = Skript.map { ByteArray(0) }): Skript<I, Http.Client.Request, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<Http.Client.Request> {
            val uriResult = uri.run(i, troupe)
            val headersResult = headers.run(i, troupe)
            val pathParametersResult = pathParameters.run(i, troupe)
            val queryParametersResult = queryParameters.run(i, troupe)
            val bodyFuture = body.run(i, troupe)
            return uriResult.flatMap { uri ->
                    headersResult.flatMap { headers ->
                    pathParametersResult.flatMap { pathParameters ->
                    queryParametersResult.map { queryParameters ->
                        Http.Client.Request( method, uri, pathParameters, queryParameters, headers, bodyFuture)
                    } } } }
        }
    }

    data class ResponseMapping<O, Troupe>(val mappers: List<Pair<IntRange, Skript<Http.Client.Response, O, Troupe>>>): Skript<Http.Client.Response, O, Troupe> {

        override fun run(i: Http.Client.Response, troupe: Troupe): AsyncResult<O> =
            mappers
                    .fold(Option.empty(), firstMatch(i))
                    .map { it.run(i, troupe) }
                    .getOrElse { AsyncResult.failed(HttpError.Client.UnhandledResponse(i)) }

        private fun firstMatch(httpClientResponse: Http.Client.Response): (Option<Skript<Http.Client.Response, O, Troupe>>, Pair<IntRange, Skript<Http.Client.Response, O, Troupe>>) -> Option<Skript<Http.Client.Response, O, Troupe>> = {
            skript, candidate ->
            skript.orElse {
                if(candidate.first.contains(httpClientResponse.status)) {
                    candidate.second.toOption()
                }    else {
                    Option.empty()
                }
            }
        }
    }
}