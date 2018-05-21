package playwrigkt.skript.user.http

import playwrigkt.skript.Skript
import playwrigkt.skript.auth.TokenAndInput
import playwrigkt.skript.http.server.getHeader
import playwrigkt.skript.http.server.getPathParameter
import playwrigkt.skript.ex.*
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.UserSkripts
import playwrigkt.skript.user.models.UserNameAndPassword
import playwrigkt.skript.user.models.UserProfile
import playwrigkt.skript.user.models.UserProfileAndPassword
import playwrigkt.skript.user.models.UserSession


object UserHttpSkripts {
    val ERROR_SERVER_RESPONSE_MAPPER: (Throwable) -> HttpServer.Response = { HttpServer.Response(Http.Status.InternalServerError, emptyMap(), AsyncResult.succeeded("Unhandled error".toByteArray())) }

    val createUser =
            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                    .flatMap  { it.body }
                    .deserialize(UserProfileAndPassword::class.java)
                    .compose(UserSkripts.createSkript)
                    .httpServerResponse(
                            Skript.map { Http.Status.Created },
                            Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                            Skript.identity<UserProfile, ApplicationTroupe>().serialize(),
                            Skript.map(ERROR_SERVER_RESPONSE_MAPPER))

    val loginUser =
            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                    .flatMap  { it.body }
                    .deserialize(UserNameAndPassword::class.java)
                    .compose(UserSkripts.loginSkript)
                    .httpServerResponse(
                            Skript.map { Http.Status.OK },
                            Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                            Skript.identity<UserSession, ApplicationTroupe>().serialize(),
                            Skript.map(ERROR_SERVER_RESPONSE_MAPPER))

    val getUser =
            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                    .both(
                            getHeader("Authorization"),
                            getPathParameter("userId"))
                    .join { authToken, userId -> TokenAndInput(authToken, userId) }
                    .compose(UserSkripts.getSkript)
                    .httpServerResponse(
                            Skript.map { Http.Status.OK },
                            Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                            Skript.identity<UserProfile, ApplicationTroupe>().serialize(),
                            Skript.map(ERROR_SERVER_RESPONSE_MAPPER))
}
