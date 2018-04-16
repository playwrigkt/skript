package playwrigkt.skript.http.client

import org.funktionale.option.Option
import org.funktionale.option.getOrElse
import org.funktionale.option.orElse
import org.funktionale.option.toOption
import playwrigkt.skript.Skript
import playwrigkt.skript.http.HttpError
import playwrigkt.skript.result.AsyncResult

data class HttpClientResponseMappingSkript<O, Troupe>(val mappers: List<Pair<IntRange, Skript<HttpClient.Response, O, Troupe>>>): Skript<HttpClient.Response, O, Troupe> {

    override fun run(i: HttpClient.Response, troupe: Troupe): AsyncResult<O> =
            mappers
                    .fold(Option.empty(), firstMatch(i))
                    .map { it.run(i, troupe) }
                    .getOrElse { AsyncResult.failed(HttpError.Client.UnhandledResponse(i)) }

    private fun firstMatch(httpClientResponse: HttpClient.Response): (Option<Skript<HttpClient.Response, O, Troupe>>, Pair<IntRange, Skript<HttpClient.Response, O, Troupe>>) -> Option<Skript<HttpClient.Response, O, Troupe>> = {
        skript, candidate ->
        skript.orElse {
            if(candidate.first.contains(httpClientResponse.status.code)) {
                candidate.second.toOption()
            }    else {
                Option.empty()
            }
        }
    }
}