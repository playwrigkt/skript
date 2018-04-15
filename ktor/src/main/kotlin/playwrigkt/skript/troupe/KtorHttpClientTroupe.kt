package playwrigkt.skript.troupe

import io.ktor.client.HttpClient
import playwrigkt.skript.performer.HttpClientPerformer
import playwrigkt.skript.performer.KtorHttpClientPerformer
import playwrigkt.skript.result.AsyncResult

class KtorHttpClientTroupe(httpClient: HttpClient): HttpClientTroupe {
    override fun getHttpRequestPerformer(): AsyncResult<out HttpClientPerformer> = clientResult.map { KtorHttpClientPerformer(it) }
    private val clientResult = AsyncResult.succeeded(httpClient)
}