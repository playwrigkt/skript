package playwrigkt.skript.user

import playwrigkt.skript.Skript
import playwrigkt.skript.auth.TokenAndInput
import playwrigkt.skript.ex.*
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.client.HttpClient
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.models.UserNameAndPassword
import playwrigkt.skript.user.models.UserProfile
import playwrigkt.skript.user.models.UserProfileAndPassword
import playwrigkt.skript.user.models.UserSession

object UserHttpClient {
    val createUserRequestSkript = Skript.identity<UserProfileAndPassword, ApplicationTroupe>()
            .httpRequest(
                    method = Http.Method.Post,
                    uri = uri(
                            useSsl = Skript.map { false },
                            host = Skript.map { "localhost" },
                            port = Skript.map { null },
                            pathTemplate = Skript.map { "/users" },
                            pathParameters = Skript.map { emptyMap() },
                            queryParameters = Skript.map { emptyMap() }),
                    body = Skript.identity<UserProfileAndPassword, ApplicationTroupe>().serialize())
            .executeRequest()
            .httpResponse(
                    Skript.identity<HttpClient.Response, ApplicationTroupe>()
                            .flatMap { it.responseBody }
                            .deserialize(UserProfile::class.java))


    val loginRequestSkript = Skript.identity<UserNameAndPassword, ApplicationTroupe>()
            .httpRequest(
                    method = Http.Method.Post,
                    uri = uri(
                            useSsl = Skript.map { false },
                            host = Skript.map { "localhost" },
                            port = Skript.map { null },
                            pathTemplate = Skript.map { "/login" },
                            pathParameters = Skript.map { emptyMap() },
                            queryParameters = Skript.map { emptyMap() }),
                    body = Skript.identity<UserNameAndPassword, ApplicationTroupe>().serialize())
            .executeRequest()
            .httpResponse(
                    Skript.identity<HttpClient.Response, ApplicationTroupe>()
                            .flatMap { it.responseBody }
                            .deserialize(UserSession::class.java))

    val getUserRequestSkript = Skript.identity<TokenAndInput<String>, ApplicationTroupe>()
            .httpRequest(
                    method = Http.Method.Get,
                    uri = uri(
                            useSsl = Skript.map { false },
                            host = Skript.map { "localhost" },
                            port = Skript.map { null },
                            pathTemplate = Skript.map { "/users/{userId}" },
                            pathParameters = Skript.map { mapOf("userId" to it.input) },
                            queryParameters = Skript.map { emptyMap() }),
                    headers = Skript.map { mapOf("Authorization" to listOf(it.token)) }
            )
            .executeRequest()
            .httpResponse(Skript.identity<HttpClient.Response, ApplicationTroupe>()
                    .flatMap { it.responseBody }
                    .deserialize(UserProfile::class.java))
}