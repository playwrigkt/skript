package playwrigkt.skript.troupe

import io.vertx.core.http.HttpClient
import playwrigkt.skript.performer.VertxHttpClientPerformer
import playwrigkt.skript.result.AsyncResult

data class VertxHttpClientTroupe(val httpClient: AsyncResult<HttpClient>): HttpClientTroupe {
    override fun getHttpRequestPerformer(): AsyncResult<VertxHttpClientPerformer> {
       return httpClient.map { VertxHttpClientPerformer(it) }
    }

}