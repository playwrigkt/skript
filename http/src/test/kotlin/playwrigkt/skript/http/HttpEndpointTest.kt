package playwrigkt.skript.http

import io.kotlintest.fail
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import playwrigkt.skript.result.AsyncResult
import java.util.*

class HttpEndpointTest: StringSpec() {
    init {
        "Match an endopint with an identical path literal" {
            HttpEndpoint("/path", emptyMap(), HttpMethod.Get)
                    .matches(HttpMethod.Get, emptyMap(), "/path") shouldBe true
        }

        "Match an endpoint with a parameterized path literal" {
            HttpEndpoint("/path/{param1}", emptyMap(), HttpMethod.Get)
                    .matches(HttpMethod.Get, emptyMap(), "/path/value1") shouldBe true
        }

        "create a request from endpoint with a parameterized path literal" {
            val request = HttpEndpoint("/path/{param1}", emptyMap(), HttpMethod.Get)
                    .request("http://localhost/path/value1",
                            HttpMethod.Get, emptyMap(),
                            AsyncResult.succeeded("".toByteArray()),
                            "/path/value1").result()

            request?.requestUri shouldBe "http://localhost/path/value1"
            request?.method shouldBe HttpMethod.Get
            request?.headers shouldBe emptyMap()
            request?.body?.result()?.let { String(it) } shouldBe ""
            request?.pathParameters shouldBe mapOf("path" to "path", "param1" to "value1")
            request?.queryParams shouldBe emptyMap<String, String>()
        }

        "Match an endpoint that requires a header" {
            HttpEndpoint("/path", mapOf("Authorization" to emptyList()), HttpMethod.Get)
                    .matches(HttpMethod.Get, mapOf("Authorization" to listOf(UUID.randomUUID().toString())), "/path") shouldBe true
        }

        "Match an endpoint that requires a header when there are extra headers" {
            HttpEndpoint("/path", mapOf("Authorization" to emptyList()), HttpMethod.Get)
                    .matches(
                            HttpMethod.Get,
                            mapOf(
                                    "Authorization" to listOf(UUID.randomUUID().toString()),
                                    "Host" to listOf("localhost")),
                            "/path") shouldBe true
        }

        "create a request from an endpoint with path parameters and headers" {
            val authHeaderValue = UUID.randomUUID().toString()
            val result =  HttpEndpoint("/path/{param1}", mapOf("Authorization" to emptyList()), HttpMethod.Get)
                    .request("http://localhost/path/value1",
                            HttpMethod.Get,
                            mapOf(
                                    "Authorization" to listOf(authHeaderValue),
                                    "Host" to listOf("localhost")),
                            AsyncResult.succeeded("".toByteArray()),
                            "/path/value1")
            if(!result.isSuccess()) fail("Expected success: ${result.error()}")
            val request = result.result()

            request?.requestUri shouldBe "http://localhost/path/value1"
            request?.method shouldBe HttpMethod.Get
            request?.headers shouldBe mapOf(
                    "Authorization" to listOf(authHeaderValue),
                    "Host" to listOf("localhost"))
            request?.body?.result()?.let { String(it) } shouldBe ""
            request?.pathParameters shouldBe mapOf("path" to "path", "param1" to "value1")
            request?.queryParams shouldBe emptyMap<String, String>()
        }


    }
}