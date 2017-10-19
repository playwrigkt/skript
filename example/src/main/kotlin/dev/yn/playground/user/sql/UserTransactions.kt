package dev.yn.playground.user.sql

import dev.yn.playground.auth.TokenAndInput
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.sql.UnpreparedSQLAction
import dev.yn.playground.task.UnpreparedVertxTask
import dev.yn.playground.task.VertxTask
import dev.yn.playground.user.models.*
import dev.yn.playground.user.userCreatedAddress
import dev.yn.playground.user.userLoginAddress
import org.funktionale.tries.Try
import java.time.Instant
import java.util.*

object UserTransactions {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }

    val createUserActionChain: UnpreparedSQLAction<UserProfileAndPassword, UserProfile, ApplicationContextProvider> =
            UnpreparedSQLAction.update<UserProfileAndPassword, UserProfileAndPassword, ApplicationContextProvider>(InsertUserProfileMapping)
                    .update(InsertUserPasswordMapping)
                    .mapTask<UserProfile>(VertxTask.sendWithResponse(userCreatedAddress))

    val loginActionChain: UnpreparedSQLAction<UserNameAndPassword, UserSession, ApplicationContextProvider> =
            UnpreparedSQLAction.query<UserNameAndPassword, UserIdAndPassword, ApplicationContextProvider>(SelectUserIdForLogin)
                    .query(ValidatePasswordForUserId)
                    .query(EnsureNoSessionExists)
                    .map(createNewSessionKey)
                    .update(InsertSession)
                    .mapTask<UserSession>(VertxTask.sendWithResponse(userLoginAddress))

    val getUserActionChain: UnpreparedSQLAction<TokenAndInput<String>, UserProfile, ApplicationContextProvider> =
            validateSession<String, ApplicationContextProvider> { session, userId ->
                if (session.userId == userId) {
                    Try.Success(userId)
                } else {
                    Try.Failure(UserError.AuthorizationFailed)
                }
            }
                    .query(SelectUserProfileById)

    private fun <T, P> validateSession(validateSession: (UserSession, T) -> Try<T>): UnpreparedSQLAction<TokenAndInput<T>, T, P> =
            UnpreparedSQLAction.query(SelectSessionByKey(validateSession))

    fun <P> deleteAllUserActionChain(): UnpreparedSQLAction<Unit, Unit, P> =
            UnpreparedSQLAction.deleteAll<Unit, P>("user_relationship_request")
                    .deleteAll { "user_password" }
                    .deleteAll { "user_session" }
                    .deleteAll { "user_profile" }
}