package dev.yn.playground.test

import dev.yn.playground.sql.UnpreparedSQLAction
import dev.yn.playground.task.UnpreparedVertxTask
import dev.yn.playground.task.VertxProvider
import dev.yn.playground.task.VertxTask
import org.funktionale.tries.Try
import java.time.Instant
import java.util.*

val userCreatedAddress = "user.created.vertx"
val userLoginAddress = "user.login.vertx"

object UserTransactions {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }

    fun <P: VertxProvider> createUserActionChain(): UnpreparedSQLAction<UserProfileAndPassword, UserProfile, P> =
            UnpreparedSQLAction.update<UserProfileAndPassword, UserProfileAndPassword, P>(InsertUserProfileMapping)
                    .update(InsertUserPasswordMapping)
                    .mapTask<UserProfile>(VertxTask.sendWithResponse(userCreatedAddress))

    fun <P: VertxProvider> loginActionChain(): UnpreparedSQLAction<UserNameAndPassword, UserSession, P> =
            UnpreparedSQLAction.query<UserNameAndPassword, UserIdAndPassword, P>(SelectUserIdForLogin)
                    .query(ValidatePasswordForUserId)
                    .query(EnsureNoSessionExists)
                    .map(createNewSessionKey)
                    .update(InsertSession)
                    .mapTask<UserSession>(VertxTask.sendWithResponse(userLoginAddress))

    fun <P> getUserActionChain(): UnpreparedSQLAction<TokenAndInput<String>, UserProfile, P> =
            validateSession<String, P> { session, userId ->
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