package dev.yn.playground.user

import dev.yn.playground.sql.UnpreparedSQLActionChain
import dev.yn.playground.task.UnpreparedVertxTask
import dev.yn.playground.task.VertxProvider
import dev.yn.playground.task.VertxTask
import org.funktionale.tries.Try
import java.time.Instant
import java.util.*

object UserTransactions {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }

    fun <P: VertxProvider> createUserActionChain(): UnpreparedSQLActionChain<UserProfileAndPassword, UserProfile, P> =
            UnpreparedSQLActionChain.update<UserProfileAndPassword, UserProfileAndPassword, P>(InsertUserProfileMapping)
                    .update(InsertUserPasswordMapping)
                    .mapTask<UserProfile>(UnpreparedVertxTask(VertxTask.sendWithResponse(userCreatedAddress)))

    fun <P: VertxProvider> loginActionChain(): UnpreparedSQLActionChain<UserNameAndPassword, UserSession, P> =
            UnpreparedSQLActionChain.query<UserNameAndPassword, UserIdAndPassword, P>(SelectUserIdForLogin)
                    .query(ValidatePasswordForUserId)
                    .query(EnsureNoSessionExists)
                    .map(createNewSessionKey)
                    .update(InsertSession)
                    .mapTask<UserSession>(UnpreparedVertxTask(VertxTask.sendWithResponse(userLoginAddress)))

    fun <P> getUserActionChain(): UnpreparedSQLActionChain<TokenAndInput<String>, UserProfile, P> =
            validateSession<String, P> { session, userId ->
                if (session.userId == userId) {
                    Try.Success(userId)
                } else {
                    Try.Failure(UserError.AuthorizationFailed)
                }
            }
                    .query(SelectUserProfileById)

    private fun <T, P> validateSession(validateSession: (UserSession, T) -> Try<T>): UnpreparedSQLActionChain<TokenAndInput<T>, T, P> =
            UnpreparedSQLActionChain.query(SelectSessionByKey(validateSession))

    fun <P> deleteAllUserActionChain(): UnpreparedSQLActionChain<Unit, Unit, P> =
            UnpreparedSQLActionChain.deleteAll<Unit, P>("user_relationship_request")
                    .deleteAll { "user_password" }
                    .deleteAll { "user_session" }
                    .deleteAll { "user_profile" }
}