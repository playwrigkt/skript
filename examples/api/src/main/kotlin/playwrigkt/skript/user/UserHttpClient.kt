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

class UserHttpClient(val configBase: String = "userHttpClient") {
    private val getHostFromConfig = Skript.identity<Any, ApplicationTroupe>()
            .map { "$configBase.host" }
            .configValue()
            .mapTry { it.text() }
            .map { it.value }

    private val getPortFromConfig = Skript.identity<Any, ApplicationTroupe>()
            .map { "$configBase.port" }
            .configValue()
            .map { it.number().map { it.value.intValueExact() }.toOption().orNull() }

    val createUserRequestSkript = Skript.identity<UserProfileAndPassword, ApplicationTroupe>()
            .httpClientRequest(
                    method = Http.Method.Post,
                    uri = uri(
                            useSsl = Skript.map { false },
                            host = getHostFromConfig,
                            port = getPortFromConfig,
                            pathTemplate = Skript.map { "/users" },
                            pathParameters = Skript.map { emptyMap() },
                            queryParameters = Skript.map { emptyMap() }),
                    body = Skript.identity<UserProfileAndPassword, ApplicationTroupe>().serialize())
            .executeRequest()
            .httpClientResponse(
                    Skript.identity<HttpClient.Response, ApplicationTroupe>()
                            .flatMap { it.responseBody }
                            .deserialize(UserProfile::class.java))


    val loginRequestSkript = Skript.identity<UserNameAndPassword, ApplicationTroupe>()
            .httpClientRequest(
                    method = Http.Method.Post,
                    uri = uri(
                            useSsl = Skript.map { false },
                            host = getHostFromConfig,
                            port = getPortFromConfig,
                            pathTemplate = Skript.map { "/login" },
                            pathParameters = Skript.map { emptyMap() },
                            queryParameters = Skript.map { emptyMap() }),
                    body = Skript.identity<UserNameAndPassword, ApplicationTroupe>().serialize())
            .executeRequest()
            .httpClientResponse(
                    Skript.identity<HttpClient.Response, ApplicationTroupe>()
                            .flatMap { it.responseBody }
                            .deserialize(UserSession::class.java))

    val getUserRequestSkript = Skript.identity<TokenAndInput<String>, ApplicationTroupe>()
            .httpClientRequest(
                    method = Http.Method.Get,
                    uri = uri(
                            useSsl = Skript.map { false },
                            host = getHostFromConfig,
                            port = getPortFromConfig,
                            pathTemplate = Skript.map { "/users/{userId}" },
                            pathParameters = Skript.map { mapOf("userId" to it.input) },
                            queryParameters = Skript.map { emptyMap() }),
                    headers = Skript.map { mapOf("Authorization" to listOf(it.token)) }
            )
            .executeRequest()
            .httpClientResponse(Skript.identity<HttpClient.Response, ApplicationTroupe>()
                    .flatMap { it.responseBody }
                    .deserialize(UserProfile::class.java))
}