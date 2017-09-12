package dev.yn.playground.user

sealed class UserError: Throwable() {
    data class NoSuchUser(val userName: String): UserError()
    data class SessionExpired(val sessionKey: String): UserError()
    data class NoSuchSession(val userSession: UserSession): UserError()
    data class NoSuchTrustedDevice(val userTrustedDevice: UserTrustedDevice): UserError()
    data class NoUserSession(val userId: String): UserError()
    data class SessionAlreadyExists(val userId: String): UserError()
    object AuthenticationFailed: UserError()
}