package playwrigkt.skript.troupe

import playwrigkt.skript.performer.HttpClientPerformer
import playwrigkt.skript.result.AsyncResult

interface HttpClientTroupe {
    fun getHttpRequestPerformer(): AsyncResult<out HttpClientPerformer>
}