package playwright.skript.user

import playwright.skript.user.models.UserProfile
import playwright.skript.user.models.UserProfileAndPassword
import java.util.*

object UserFixture {
    fun generateUser(num: Int): UserProfileAndPassword {
        val userId = UUID.randomUUID().toString()
        val password = "pass$num"
        val userName = "user$num"
        val user = UserProfile(userId, userName, false)
        return UserProfileAndPassword(user, password)
    }
}