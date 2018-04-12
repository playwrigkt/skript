package playwrigkt.skript.troupe

import playwrigkt.skript.performer.HttpRequestPerformer
import playwrigkt.skript.result.AsyncResult

interface HttpRequestTroupe {
    fun getHttpRequestPerformer(): AsyncResult<out HttpRequestPerformer>
}