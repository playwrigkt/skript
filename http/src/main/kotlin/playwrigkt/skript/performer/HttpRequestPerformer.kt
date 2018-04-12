package playwrigkt.skript.performer

import playwrigkt.skript.http.HttpClientRequest
import playwrigkt.skript.http.HttpClientResponse
import playwrigkt.skript.result.AsyncResult

interface HttpRequestPerformer {
    fun perform(httpClientRequest: HttpClientRequest): AsyncResult<HttpClientResponse>
}