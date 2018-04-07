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

val errorResponseMapper: (Throwable) -> HttpResponse = { HttpResponse(500, "error".toByteArray()) }

val createUserHttpEndpointSkript =
        Skript.identity<HttpRequest<ByteArray>, ApplicationTroupe>()
                .andThen(HttpRequestDeserializationSkript(UserProfileAndPassword::class.java))
                .flatMapWithTroupe { request, _ -> request.body }
                .compose(UserSkripts.UNPREPARED_CREATE_SKRIPT)
                .compose(HttpResponseSerializationSkript(errorResponseMapper))

val loginUserHttpEndpointSkript =
        Skript.identity<HttpRequest<ByteArray>, ApplicationTroupe>()
                .andThen(HttpRequestDeserializationSkript(UserNameAndPassword::class.java))
                .flatMapWithTroupe { request, _ -> request.body }
                .compose(UserSkripts.UNPREPARED_LOGIN_SKRIPT)
                .compose(HttpResponseSerializationSkript(errorResponseMapper))

val getUserHttpEndpointSkript =
        Skript.identity<HttpRequest<ByteArray>, ApplicationTroupe>()
                .mapTry {
                    Try {it.headers.get("Authorization")
                        ?.firstOrNull()
                        ?.let { authHeader ->
                            it.pathParameters.get("userId")
                                    ?.let { TokenAndInput(authHeader, it) }
                        }?:throw HttpError.MissingInputs(listOf(HttpError.HttpInput("path", "userId")))
                    }
                }
                .compose(UserSkripts.UNPREPARED_GET_SKRIPT)
                .compose(HttpResponseSerializationSkript(errorResponseMapper))