package dev.yn.playground.user

data class User(val id: String, val name: String, val allowPubliMessage: Boolean)
data class UserNameAndPassword(val userName: String, val password: String)
data class UserIdAndPassword(val id: String, val password: String)
data class UserAndPassword(val user: User, val password: String)