package dev.yn.playground.user

import java.util.*

object UserFixture {
    fun generateUser(num: Int): UserProfileAndPassword {
        val userId = UUID.randomUUID().toString()
        val password = "pass$num"
        val userName = "user$num"
        val user = dev.yn.playground.user.UserProfile(userId, userName, false)
        return UserProfileAndPassword(user, password)
    }
}