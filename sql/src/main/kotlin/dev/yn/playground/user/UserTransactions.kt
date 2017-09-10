package dev.yn.playground.user

import dev.yn.playground.sql.SQLTransaction
import dev.yn.playground.sql.extensions.transaction.*
import java.time.Instant
import java.util.*

object UserTransactions {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now()) }

    val createUserTransaction =
            update(InsertUserProfileMapping)
                    .update(InsertUserPasswordMapping)

    val login: SQLTransaction<UserNameAndPassword, UserIdAndPassword, UserSession> =
            query(SelectUserIdForLogin)
                    .query(ValidatePasswordForUserId)
                    .query(EnsureNoSessionExists)
                    .map(createNewSessionKey)
                    .update(InsertSession)

    val validateSession: SQLTransaction<UserSession, UserSession, UserSession> =
            query(SelectSessionByKeyAnddUserId)

    val deleteAllUsersTransaction: SQLTransaction<Unit, Unit, Unit> =
            deleteAll("user_password")
                    .deleteAll { "user_session" }
                    .deleteAll {"user_profile"}

}
