package playwrigkt.skript.chatroom

import playwrigkt.skript.Skript
import playwrigkt.skript.auth.TokenAndInput
import playwrigkt.skript.chatroom.models.ChatRoom
import playwrigkt.skript.chatroom.models.ChatRoomUser
import playwrigkt.skript.ex.*
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.client.HttpClient
import playwrigkt.skript.troupe.ApplicationTroupe

class ChatRoomHttpClient(val configBase: String = "chatRoomHttpClient") {
    private val getHostFromConfig = Skript.identity<Any, ApplicationTroupe>()
            .map { "$configBase.host" }
            .configValue()
            .mapTry { it.text() }
            .map { it.value }

    private val getPortFromConfig = Skript.identity<Any, ApplicationTroupe>()
            .map { "$configBase.port" }
            .configValue()
            .map { it.number().map { it.value.intValueExact() }.toOption().orNull() }

    val createChatRoom = Skript.identity<TokenAndInput<ChatRoom>, ApplicationTroupe>()
            .httpClientRequest(
                    method = Http.Method.Post,
                    uri = uri(
                            useSsl = Skript.map { false },
                            host = getHostFromConfig,
                            port = getPortFromConfig,
                            pathTemplate = Skript.map { "/chatRooms" },
                            pathParameters = Skript.map { emptyMap() },
                            queryParameters = Skript.map { emptyMap() }),
                    headers = Skript.map { mapOf("Authorization" to listOf(it.token)) },
                    body = Skript.identity<TokenAndInput<ChatRoom>, ApplicationTroupe>().map { it.input }.serialize())
            .executeRequest()
            .httpClientResponse(
                    Skript.identity<HttpClient.Response, ApplicationTroupe>()
                            .flatMap { it.responseBody }
                            .deserialize(ChatRoom::class.java))


    val getChatRoom = Skript.identity<TokenAndInput<String>, ApplicationTroupe>()
            .httpClientRequest(
                    method = Http.Method.Get,
                    uri = uri(
                            useSsl = Skript.map { false },
                            host = getHostFromConfig,
                            port = getPortFromConfig,
                            pathTemplate = Skript.map { "/chatRooms/{chatRoomId}" },
                            pathParameters = Skript.map { mapOf("chatRoomId" to it.input) },
                            queryParameters = Skript.map { emptyMap() }),
                    headers = Skript.map { mapOf("Authorization" to listOf(it.token)) }
            )
            .executeRequest()
            .httpClientResponse(Skript.identity<HttpClient.Response, ApplicationTroupe>()
                    .flatMap { it.responseBody }
                    .deserialize(ChatRoom::class.java))

    val updateChatRoom = Skript.identity<TokenAndInput<ChatRoom>, ApplicationTroupe>()
            .httpClientRequest(
                    method = Http.Method.Put,
                    uri = uri(
                            useSsl = Skript.map { false },
                            host = getHostFromConfig,
                            port = getPortFromConfig,
                            pathTemplate = Skript.map { "/chatRoom/{chatRoomId}" },
                            pathParameters = Skript.map { mapOf("chatRoomId" to it.input.id) },
                            queryParameters = Skript.map { emptyMap() }),
                    headers = Skript.map { mapOf("Authorization" to listOf(it.token)) },
                    body = Skript.identity<TokenAndInput<ChatRoom>, ApplicationTroupe>().map { it.input }.serialize())
            .executeRequest()
            .httpClientResponse(
                    Skript.identity<HttpClient.Response, ApplicationTroupe>()
                            .flatMap { it.responseBody }
                            .deserialize(ChatRoom::class.java))

    val addUserPermissions = Skript.identity<TokenAndInput<ChatRoomUser>, ApplicationTroupe>()
            .httpClientRequest(
                    method = Http.Method.Put,
                    uri = uri(
                            useSsl = Skript.map { false },
                            host = getHostFromConfig,
                            port = getPortFromConfig,
                            pathTemplate = Skript.map { "/chatRooms/{chatRoomId}/permissions/{userId}" },
                            pathParameters = Skript.map { mapOf(
                                    "chatRoomId" to it.input.chatroom.id,
                                    "userId" to it.input.user.id)
                            },
                            queryParameters = Skript.map { emptyMap() }),
                    headers = Skript.map { mapOf("Authorization" to listOf(it.token)) },
                    body = Skript.identity<TokenAndInput<ChatRoomUser>, ApplicationTroupe>().map { it.input.permissions }.serialize())
            .executeRequest()
            .httpClientResponse(
                    Skript.identity<HttpClient.Response, ApplicationTroupe>()
                            .flatMap { it.responseBody }
                            .deserialize(ChatRoom::class.java))

    val removeUserPermissions = Skript.identity<TokenAndInput<ChatRoomUser>, ApplicationTroupe>()
            .httpClientRequest(
                    method = Http.Method.Delete,
                    uri = uri(
                            useSsl = Skript.map { false },
                            host = getHostFromConfig,
                            port = getPortFromConfig,
                            pathTemplate = Skript.map { "/chatRooms/{chatRoomId}/permissions/{userId}" },
                            pathParameters = Skript.map { mapOf(
                                    "chatRoomId" to it.input.chatroom.id,
                                    "userId" to it.input.user.id)
                            },
                            queryParameters = Skript.map { emptyMap() }),
                    headers = Skript.map { mapOf("Authorization" to listOf(it.token)) },
                    body = Skript.identity<TokenAndInput<ChatRoomUser>, ApplicationTroupe>().map { it.input.permissions }.serialize())
            .executeRequest()
            .httpClientResponse(
                    Skript.identity<HttpClient.Response, ApplicationTroupe>()
                            .flatMap { it.responseBody }
                            .deserialize(ChatRoom::class.java))
}