package playwrigkt.skript.user

import playwrigkt.skript.user.models.UserProfile
import playwrigkt.skript.user.models.UserProfileAndPassword
import java.util.*

object UserFixture {
    val random = Random()
    fun generateUser(): UserProfileAndPassword = generateUser(random.nextInt())

    fun generateUser(num: Int): UserProfileAndPassword {
        val userId = UUID.randomUUID().toString()
        val password = "pass$num"
        val userName = "user$num"
        val user = UserProfile(userId, userName, false)
        return UserProfileAndPassword(user, password)
    }
}