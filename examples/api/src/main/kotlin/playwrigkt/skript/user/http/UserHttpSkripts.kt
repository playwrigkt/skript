package playwrigkt.skript.user.http

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.auth.TokenAndInput
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.ex.httpServerResponse
import playwrigkt.skript.ex.join
import playwrigkt.skript.ex.serialize
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.HttpError
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.http.server.HttpServerRequestDeserializationSkript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.UserSkripts
import playwrigkt.skript.user.models.UserNameAndPassword
import playwrigkt.skript.user.models.UserProfile
import playwrigkt.skript.user.models.UserProfileAndPassword
import playwrigkt.skript.user.models.UserSession

val ERROR_SERVER_RESPONSE_MAPPER: (Throwable) -> HttpServer.Response = { HttpServer.Response(Http.Status.InternalServerError, emptyMap(), AsyncResult.succeeded("Unhandled error".toByteArray())) }

val createUserHttpEndpointSkript =
        Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                .andThen(HttpServerRequestDeserializationSkript(UserProfileAndPassword::class.java))
                .flatMapWithTroupe { request, _ -> request.body }
                .compose(UserSkripts.createSkript)
                .httpServerResponse(
                        Skript.map { Http.Status.Created },
                        Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                        Skript.identity<UserProfile, ApplicationTroupe>().serialize(),
                        Skript.map(ERROR_SERVER_RESPONSE_MAPPER))

val loginUserHttpEndpointSkript =
        Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                .andThen(HttpServerRequestDeserializationSkript(UserNameAndPassword::class.java))
                .flatMapWithTroupe { request, _ -> request.body }
                .compose(UserSkripts.loginSkript)
                .httpServerResponse(
                        Skript.map { Http.Status.OK },
                        Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                        Skript.identity<UserSession, ApplicationTroupe>().serialize(),
                        Skript.map(ERROR_SERVER_RESPONSE_MAPPER))

val getUserHttpEndpointSkript =
        Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                .both<String, String>(
                        Skript.mapTry {
                            it.headers.get("Authorization")
                                    ?.firstOrNull()
                                    ?.let { Try.Success(it) }
                            ?: Try.Failure(HttpError.MissingInputs(listOf(HttpError.HttpInput.header("Authorization"))))
                        },
                        Skript.mapTry {
                                it.pathParameters.get("userId")
                                        ?.let { Try.Success(it) }
                                        ?:Try.Failure(HttpError.MissingInputs(listOf(HttpError.HttpInput.path("userId"))))
                        })
                .join { authToken, userId -> TokenAndInput(authToken, userId) }
                .compose(UserSkripts.getSkript)
                .httpServerResponse(
                        Skript.map { Http.Status.OK },
                        Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                        Skript.identity<UserProfile, ApplicationTroupe>().serialize(),
                        Skript.map(ERROR_SERVER_RESPONSE_MAPPER))