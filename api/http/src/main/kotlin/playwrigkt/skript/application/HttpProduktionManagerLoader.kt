package playwrigkt.skript.application

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.ex.lift
import playwrigkt.skript.ex.liftTry
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.server.HttpServer

object HttpProduktionManagerLoader: ProduktionManagerLoader<HttpServer.Endpoint, HttpServer.Request<ByteArray>, HttpServer.Response, Any>() {
    override val parseRuleConfig: Skript<ConfigValue, HttpServer.Endpoint, SkriptApplicationLoader> = Skript.mapTry { it.toHttpEndpoint() }

    fun ConfigValue.toHttpEndpoint(): Try<HttpServer.Endpoint> =
            this.applyPath("path", ".")
                    .flatMap { it.text() }
                    .map { it.value }
                    .flatMap { path ->
                        this.applyPath("headers", ".")
                                .flatMap { it.objekt() }
                                .flatMap { it.values
                                        .mapValues {
                                            it.value
                                                    .array()
                                                    .flatMap { it.list.map { it.text().map { it.value } }.liftTry() }
                                        }
                                        .lift()
                                }
                                .flatMap {  headers ->
                                    this.applyPath("method", ".")
                                            .flatMap { it.toHttpMethod() }
                                            .map { HttpServer.Endpoint(path, headers, it) }

                                }
                    }

    fun ConfigValue.toHttpMethod(): Try<Http.Method> =
            this.text().map { when(it.value.toLowerCase()) {
                "get" -> Http.Method.Get
                "post" -> Http.Method.Post
                "put" -> Http.Method.Put
                "delete" -> Http.Method.Delete
                "options" -> Http.Method.Options
                "head" -> Http.Method.Head
                "trace" -> Http.Method.Trace
                "connect" -> Http.Method.Connect
                "patch" -> Http.Method.Patch
                else -> Http.Method.Other(it.value)
            } }

}