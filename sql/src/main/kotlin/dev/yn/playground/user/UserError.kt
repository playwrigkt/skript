package dev.yn.playground.user

sealed class UserError: Throwable() {
    data class NoSuchUser(val userName: String): UserError()
    object AuthenticationFailed: UserError()
}