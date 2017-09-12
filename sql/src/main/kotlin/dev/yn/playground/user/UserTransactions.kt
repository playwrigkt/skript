package dev.yn.playground.user

import dev.yn.playground.sql.SQLTransaction
import java.time.Instant
import java.util.*

object UserTransactions {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now()) }

    val createUserTransaction =
            SQLTransaction.update(InsertUserProfileMapping)
                    .update(InsertUserPasswordMapping)

    val login: SQLTransaction<UserNameAndPassword, UserSession> =
            SQLTransaction.query(SelectUserIdForLogin)
                    .query(ValidatePasswordForUserId)
                    .query(EnsureNoSessionExists)
                    .map(createNewSessionKey)
                    .update(InsertSession)

    val validateSession: SQLTransaction<UserSession, UserSession> =
            SQLTransaction.query(SelectSessionByKeyAnddUserId)

    val deleteAllUsersTransaction: SQLTransaction<Unit, Unit> =
            SQLTransaction.deleteAll<Unit>("user_relationship_request")
                    .deleteAll { "user_password" }
                    .deleteAll { "user_session" }
                    .deleteAll { "user_profile" }

}
