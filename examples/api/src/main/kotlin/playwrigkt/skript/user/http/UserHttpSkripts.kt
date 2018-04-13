package playwrigkt.skript.user.http

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.auth.TokenAndInput
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.http.*
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.UserSkripts
import playwrigkt.skript.user.models.UserNameAndPassword
import playwrigkt.skript.user.models.UserProfileAndPassword

val ERROR_SERVER_RESPONSE_MAPPER: (Throwable) -> Http.Server.Response = { Http.Server.Response(500, "Internal Server Error", emptyMap(), "error".toByteArray()) }

val createUserHttpEndpointSkript =
        Skript.identity<Http.Server.Request<ByteArray>, ApplicationTroupe>()
                .andThen(HttpRequestDeserializationSkript(UserProfileAndPassword::class.java))
                .flatMapWithTroupe { request, _ -> request.body }
                .compose(UserSkripts.createSkript)
                .compose(HttpResponseSerializationSkript(ERROR_SERVER_RESPONSE_MAPPER))

val loginUserHttpEndpointSkript =
        Skript.identity<Http.Server.Request<ByteArray>, ApplicationTroupe>()
                .andThen(HttpRequestDeserializationSkript(UserNameAndPassword::class.java))
                .flatMapWithTroupe { request, _ -> request.body }
                .compose(UserSkripts.loginSkript)
                .compose(HttpResponseSerializationSkript(ERROR_SERVER_RESPONSE_MAPPER))

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
                .compose(HttpResponseSerializationSkript(ERROR_SERVER_RESPONSE_MAPPER))