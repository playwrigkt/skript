package playwrigkt.skript.http.client

import playwrigkt.skript.Skript
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.HttpClientTroupe
import playwrigkt.skript.troupe.SerializeTroupe

class HttpClientSkript: Skript<HttpClient.Request, HttpClient.Response, HttpClientTroupe> {
    companion object {
        fun <I, O, Troupe> serialized(toRequest: Skript<I, HttpClient.Request, Troupe>,
                                      fromResponse: Skript<HttpClient.Response, O, Troupe>): Skript<I, O, Troupe> where Troupe: HttpClientTroupe, Troupe : SerializeTroupe =
            Skript.identity<I, Troupe>()
                    .andThen(toRequest)
                    .andThen(HttpClientSkript())
                    .andThen(fromResponse)
    }

    override fun run(i: HttpClient.Request, troupe: HttpClientTroupe): AsyncResult<HttpClient.Response> =
        troupe.getHttpRequestPerformer().flatMap { it.perform(i) }

}