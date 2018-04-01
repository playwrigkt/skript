package playwright.skript.consumer.alpha

import playwrigkt.skript.Skript
import playwrigkt.skript.consumer.alpha.Production
import playwrigkt.skript.consumer.alpha.Venue
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.venue.StageManager

abstract class HttpVenue: Venue<HttpEndpoint, HttpRequest> {
    override fun <O, STAGE> sink(skript: Skript<HttpRequest, O, STAGE>,
                                 stageManager: StageManager<STAGE>,
                                 rule: HttpEndpoint): AsyncResult<Production> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
data class HttpEndpoint(
        val path: String = "/",
        val headers: Map<String, List<String>>,
        val method: HttpMethod
)

sealed class HttpMethod {
    fun matches(other: HttpMethod) =
            when(other) {
                is All -> true
                else -> this.equals(other)
            }

    object Get: HttpMethod()
    object Put: HttpMethod();
    object Delete: HttpMethod();
    object Post: HttpMethod()
    object Head: HttpMethod()
    object Options: HttpMethod()
    object Connect: HttpMethod()
    object All: HttpMethod() {
        override fun equals(other: Any?): Boolean =
                when(other) {
                    is HttpMethod -> true
                    else -> false
                }
    }
}

data class HttpRequest(
        val requestUri: String,
        val method: HttpMethod,
        val headers: Map<String, List<String>>,
        val body: ByteArray
)

data class HttpResponse(
        val status: Int,
        val responseBody: ByteArray
)

class HttpProduction<STAGE>(
        val stageManager: StageManager<STAGE>,
        val skript: Skript<HttpRequest, HttpResponse, STAGE>): Production {
    override fun isRunning(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stop(): AsyncResult<Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun result(): AsyncResult<Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}