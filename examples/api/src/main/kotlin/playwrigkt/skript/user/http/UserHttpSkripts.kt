package playwrigkt.skript.user.http

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.auth.TokenAndInput
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.ex.serialize
import playwrigkt.skript.http.*
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.UserSkripts
import playwrigkt.skript.user.models.UserNameAndPassword
import playwrigkt.skript.user.models.UserProfile
import playwrigkt.skript.user.models.UserProfileAndPassword
import playwrigkt.skript.user.models.UserSession

val ERROR_SERVER_RESPONSE_MAPPER: (Throwable) -> Http.Server.Response = { Http.Server.Response(Http.Status.InternalServerError, emptyMap(), AsyncResult.succeeded("Unhandled error".toByteArray())) }

val createUserHttpEndpointSkript =
        Skript.identity<Http.Server.Request<ByteArray>, ApplicationTroupe>()
                .andThen(HttpServerRequestDeserializationSkript(UserProfileAndPassword::class.java))
                .flatMapWithTroupe { request, _ -> request.body }
                .compose(UserSkripts.createSkript)
                .compose(HttpServerResponseSerializationSkript(
                        Skript.map { Http.Status.Created },
                        Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                        Skript.identity<UserProfile, ApplicationTroupe>().serialize(),
                        Skript.map(ERROR_SERVER_RESPONSE_MAPPER)))

val loginUserHttpEndpointSkript =
        Skript.identity<Http.Server.Request<ByteArray>, ApplicationTroupe>()
                .andThen(HttpServerRequestDeserializationSkript(UserNameAndPassword::class.java))
                .flatMapWithTroupe { request, _ -> request.body }
                .compose(UserSkripts.loginSkript)
                .compose(HttpServerResponseSerializationSkript(
                        Skript.map { Http.Status.OK },
                        Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                        Skript.identity<UserSession, ApplicationTroupe>().serialize(),
                        Skript.map(ERROR_SERVER_RESPONSE_MAPPER)))

val getUserHttpEndpointSkript =
        Skript.identity<Http.Server.Request<ByteArray>, ApplicationTroupe>()
                .mapTry {
                    Try {it.headers.get("Authorization")
                        ?.firstOrNull()
                        ?.let { authHeader ->
                            it.pathParameters.get("userId")
                                    ?.let { TokenAndInput(authHeader, it) }
                        }?:throw HttpError.MissingInputs(listOf(HttpError.HttpInput("path", "userId")))
                    }
                }
                .compose(UserSkripts.getSkript)
                .compose(HttpServerResponseSerializationSkript(
                        Skript.map { Http.Status.OK },
                        Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                        Skript.identity<UserProfile, ApplicationTroupe>().serialize(),
                        Skript.map(ERROR_SERVER_RESPONSE_MAPPER)))