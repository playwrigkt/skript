package playwrigkt.skript.http

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.HttpRequestTroupe

class HttpRequestSkript: Skript<HttpClientRequest, HttpClientResponse, HttpRequestTroupe> {
    override fun run(i: HttpClientRequest, troupe: HttpRequestTroupe): AsyncResult<HttpClientResponse> =
        troupe.getHttpRequestPerformer().flatMap { it.perform(i) }
}