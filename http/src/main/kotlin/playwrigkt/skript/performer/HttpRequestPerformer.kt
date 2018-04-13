package playwrigkt.skript.performer

import playwrigkt.skript.http.Http
import playwrigkt.skript.result.AsyncResult

interface HttpRequestPerformer {
    fun perform(httpClientRequest: Http.Client.Request): AsyncResult<Http.Client.Response>
}