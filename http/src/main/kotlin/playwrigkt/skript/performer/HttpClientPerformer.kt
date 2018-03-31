package playwrigkt.skript.performer

import playwrigkt.skript.http.client.HttpClient
import playwrigkt.skript.result.AsyncResult

interface HttpClientPerformer {
    fun perform(httpClientRequest: HttpClient.Request): AsyncResult<HttpClient.Response>
}