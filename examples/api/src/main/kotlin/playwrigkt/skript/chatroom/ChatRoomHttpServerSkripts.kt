package playwrigkt.skript.chatroom

import playwrigkt.skript.Skript
import playwrigkt.skript.auth.TokenAndInput
import playwrigkt.skript.http.server.getHeader
import playwrigkt.skript.http.server.getPathParameter
import playwrigkt.skript.chatroom.models.ChatRoom
import playwrigkt.skript.chatroom.models.ChatRoomPermissions
import playwrigkt.skript.chatroom.models.ChatRoomUser
import playwrigkt.skript.common.models.Reference
import playwrigkt.skript.ex.*
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.server.HttpServer
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.http.UserHttpSkripts

object ChatRoomHttpServerSkripts {
    val getChatRoom: Skript<HttpServer.Request<ByteArray>, HttpServer.Response, ApplicationTroupe> =
            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                    .both(
                            getHeader("Authorization"),
                            getPathParameter("chatRoomId"))
                    .join { authToken, chatRoomId -> TokenAndInput(authToken, chatRoomId) }
                    .compose(ChatRoomSkripts.GET_CHAT_ROOM_TRANSACTION)
                    .httpServerResponse(
                            Skript.map { Http.Status.OK },
                            Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                            Skript.identity<ChatRoom, ApplicationTroupe>().serialize(),
                            Skript.map(UserHttpSkripts.ERROR_SERVER_RESPONSE_MAPPER))

    val createChatRoom: Skript<HttpServer.Request<ByteArray>, HttpServer.Response, ApplicationTroupe> =
            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                    .both(
                            getHeader("Authorization"),
                            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                                    .flatMap { it.body }
                                    .deserialize(ChatRoom::class.java)
                    ).join { authToken, chatRoom -> TokenAndInput(authToken, chatRoom)}
                    .compose(ChatRoomSkripts.CREATE_CHAT_ROOM_TRANSACTION)
                    .httpServerResponse(
                            Skript.map { Http.Status.OK },
                            Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                            Skript.identity<ChatRoom, ApplicationTroupe>().serialize(),
                            Skript.map(UserHttpSkripts.ERROR_SERVER_RESPONSE_MAPPER))

    val updateChatRoom: Skript<HttpServer.Request<ByteArray>, HttpServer.Response, ApplicationTroupe> =
            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                    .all(
                            getHeader("Authorization"),
                            getPathParameter("chatRoomId"),
                            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                                    .flatMap { it.body }
                                    .deserialize(ChatRoom::class.java)
                    ).join { authToken, chatRoomId, chatRoom -> TokenAndInput(authToken, chatRoom.copy(id = chatRoomId))}
                    .compose(ChatRoomSkripts.UPDATE_CHAT_ROOM_TRANSACTION)
                    .httpServerResponse(
                            Skript.map { Http.Status.OK },
                            Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                            Skript.identity<ChatRoom, ApplicationTroupe>().serialize(),
                            Skript.map(UserHttpSkripts.ERROR_SERVER_RESPONSE_MAPPER))

    val addUserPermissions: Skript<HttpServer.Request<ByteArray>, HttpServer.Response, ApplicationTroupe> =
            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                    .all(
                            getHeader("Authorization"),
                            getPathParameter("chatRoomId"),
                            getPathParameter("userId"),
                            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                                    .flatMap { it.body }
                                    .deserialize(Set::class.java)
                                    .map { it.mapNotNull { it.toString() }.toSet() }
                    )
                    .join { authToken, chatRoomId, userId, permissions -> TokenAndInput(authToken, ChatRoomUser(Reference.empty(userId), Reference.empty(chatRoomId), permissions))}
                    .compose(ChatRoomSkripts.ADD_USER_PERMISSIONS)
                    .httpServerResponse(
                            Skript.map { Http.Status.OK },
                            Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                            Skript.identity<ChatRoom, ApplicationTroupe>().serialize(),
                            Skript.map(UserHttpSkripts.ERROR_SERVER_RESPONSE_MAPPER))

    val removeUserPermissions: Skript<HttpServer.Request<ByteArray>, HttpServer.Response, ApplicationTroupe> =
            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                    .all(
                            getHeader("Authorization"),
                            getPathParameter("chatRoomId"),
                            getPathParameter("userId"),
                            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                                    .flatMap { it.body }
                                    .deserialize(Set::class.java)
                                    .map { it.mapNotNull { it.toString() }.toSet() }
                    )
                    .join { authToken, chatRoomId, userId, permissions -> TokenAndInput(authToken, ChatRoomUser(Reference.empty(userId), Reference.empty(chatRoomId), permissions))}
                    .compose(ChatRoomSkripts.REMOVE_USER_PERMISSIONS)
                    .httpServerResponse(
                            Skript.map { Http.Status.OK },
                            Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                            Skript.identity<ChatRoom, ApplicationTroupe>().serialize(),
                            Skript.map(UserHttpSkripts.ERROR_SERVER_RESPONSE_MAPPER))

    val addPublicPermissions: Skript<HttpServer.Request<ByteArray>, HttpServer.Response, ApplicationTroupe> =
            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                    .all(
                            getHeader("Authorization"),
                            getPathParameter("chatRoomId"),
                            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                                    .flatMap { it.body }
                                    .deserialize(Set::class.java)
                                    .map { it.mapNotNull { it.toString() }.toSet() }
                    )
                    .join { authToken, chatRoomId, permissions -> TokenAndInput(authToken, ChatRoomPermissions(Reference.empty(chatRoomId), permissions))}
                    .compose(ChatRoomSkripts.ADD_PERMISSIONS)
                    .httpServerResponse(
                            Skript.map { Http.Status.OK },
                            Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                            Skript.identity<ChatRoom, ApplicationTroupe>().serialize(),
                            Skript.map(UserHttpSkripts.ERROR_SERVER_RESPONSE_MAPPER))

    val removePublicPermissions: Skript<HttpServer.Request<ByteArray>, HttpServer.Response, ApplicationTroupe> =
            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                    .all(
                            getHeader("Authorization"),
                            getPathParameter("chatRoomId"),
                            Skript.identity<HttpServer.Request<ByteArray>, ApplicationTroupe>()
                                    .flatMap { it.body }
                                    .deserialize(Set::class.java)
                                    .map { it.mapNotNull { it.toString() }.toSet() }
                    )
                    .join { authToken, chatRoomId, permissions -> TokenAndInput(authToken, ChatRoomPermissions(Reference.empty(chatRoomId), permissions))}
                    .compose(ChatRoomSkripts.REMOVE_PERMISSIONS)
                    .httpServerResponse(
                            Skript.map { Http.Status.OK },
                            Skript.map { mapOf("Content-Type" to listOf("application/json")) },
                            Skript.identity<ChatRoom, ApplicationTroupe>().serialize(),
                            Skript.map(UserHttpSkripts.ERROR_SERVER_RESPONSE_MAPPER))
}