package playwrigkt.skript.stagemanager

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.KtorHttpClientTroupe

class KtorHttpClientStageManager(): StageManager<KtorHttpClientTroupe> {
    private val httpClient by lazy {
        HttpClient(Apache)
    }

    override fun hireTroupe(): KtorHttpClientTroupe = KtorHttpClientTroupe(httpClient)

    override fun tearDown(): AsyncResult<Unit> = runAsync { httpClient.close() }
}