package playwright.skript.consumer.alpha

import playwrigkt.skript.Skript
import playwrigkt.skript.consumer.alpha.*
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.venue.Venue

abstract class HttpConsumerPerformer<STAGE>(
        val venue: Venue<STAGE>): ConsumerPerformer<STAGE, HttpRequest> {
    override fun <O> sink(skript: Skript<HttpRequest, O, STAGE>): AsyncResult<Sink> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <O> stream(skript: Skript<HttpRequest, O, STAGE>): AsyncResult<Stream<O>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
data class HttpEndpoint(
        val path: String = "/",
        val headers: Map<String, List<String>>,
        val method: HttpMethod
)

sealed class HttpMethod {
    object Get: HttpMethod()
    object Put: HttpMethod();
    object Delete: HttpMethod();
    object Post: HttpMethod()
    object Head: HttpMethod()
    object Options: HttpMethod()
    object Connect: HttpMethod()
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

class HttpConsumer<STAGE>(
        val venue: Venue<STAGE>,
        val skript: Skript<HttpRequest, HttpResponse, STAGE>): Consumer {
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