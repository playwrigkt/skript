package dev.yn.playground.user

import dev.yn.playground.sql.SQLTransaction
import org.funktionale.tries.Try
import java.time.Instant
import java.util.*

object UserTransactions {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }

    val createUserTransaction: SQLTransaction<UserProfileAndPassword, UserProfile> =
            SQLTransaction.update(InsertUserProfileMapping)
                    .update(InsertUserPasswordMapping)

    val loginTransaction: SQLTransaction<UserNameAndPassword, UserSession> =
            SQLTransaction.query(SelectUserIdForLogin)
                    .query(ValidatePasswordForUserId)
                    .query(EnsureNoSessionExists)
                    .map(createNewSessionKey)
                    .update(InsertSession)

    val getUserTransaction: SQLTransaction<TokenAndInput<String>, UserProfile> =
            validateSession<String> { session, userId ->
                if(session.userId == userId) { Try.Success(userId) }
                else { Try.Failure(UserError.AuthorizationFailed) }
            }
                    .query(SelectUserProfileById)

    private fun <T> validateSession(validateSession: (UserSession, T) -> Try<T>): SQLTransaction<TokenAndInput<T>, T> =
            SQLTransaction.query(SelectSessionByKey(validateSession))

    val deleteAllUsersTransaction: SQLTransaction<Unit, Unit> =
            SQLTransaction.deleteAll<Unit>("user_relationship_request")
                    .deleteAll { "user_password" }
                    .deleteAll { "user_session" }
                    .deleteAll { "user_profile" }
}