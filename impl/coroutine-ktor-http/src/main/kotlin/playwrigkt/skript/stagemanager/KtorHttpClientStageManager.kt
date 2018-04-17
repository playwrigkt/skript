package playwrigkt.skript.stagemanager

import io.ktor.client.HttpClient
import playwrigkt.skript.troupe.KtorHttpClientTroupe

//TODO handle http client lifecycle
class KtorHttpClientStageManager(val httpClient: HttpClient): StageManager<KtorHttpClientTroupe> {
    override fun hireTroupe(): KtorHttpClientTroupe = KtorHttpClientTroupe(httpClient)
}