package dev.yn.playground.user

import dev.yn.playground.Task
import dev.yn.playground.auth.TokenAndInput
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.ex.*
import dev.yn.playground.publisher.PublishCommand
import dev.yn.playground.user.models.*
import dev.yn.playground.user.sql.*
import devyn.playground.sql.task.SQLTransactionTask
import org.funktionale.tries.Try
import java.time.Instant
import java.util.*

object UserTasks {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }

    private val publishUserCreateEvent: Task<UserProfile, UserProfile, ApplicationContext> =
            Task.identity<UserProfile, ApplicationContext>()
                    .serialize()
                    .publish { PublishCommand.Publish(userCreatedAddress, it) }
                    .deserialize(UserProfile::class.java)

    private val publishUserLoginEvent: Task<UserSession, UserSession, ApplicationContext> =
            Task.identity<UserSession, ApplicationContext>()
                    .serialize()
                    .publish { PublishCommand.Publish(userLoginAddress, it) }
                    .deserialize(UserSession::class.java)

    private val onlyIfRequestedUserMatchesSessionUser =  { session: UserSession, userId: String ->
        if (session.userId == userId) {
            Try.Success(userId)
        } else {
            Try.Failure<String>(UserError.AuthorizationFailed)
        }
    }

    val unpreparedCreateTask: Task<UserProfileAndPassword, UserProfile, ApplicationContext> =
            SQLTransactionTask.transaction(
                    Task.identity<UserProfileAndPassword, ApplicationContext>()
                            .update(InsertUserProfileMapping)
                            .update(InsertUserPasswordMapping)
                            .andThen(publishUserCreateEvent))

    val unpreparedLoginTask: Task<UserNameAndPassword, UserSession, ApplicationContext> =
            SQLTransactionTask.transaction(
                    Task.identity<UserNameAndPassword, ApplicationContext>()
                            .query(SelectUserIdForLogin)
                            .query(ValidatePasswordForUserId)
                            .query(EnsureNoSessionExists)
                            .map(createNewSessionKey)
                            .update(InsertSession)
                            .andThen(publishUserLoginEvent))


    val unpreparedGetTask: Task<TokenAndInput<String>, UserProfile, ApplicationContext> =
            SQLTransactionTask.autoCommit(
                    validateSession<String>(onlyIfRequestedUserMatchesSessionUser)
                            .query(SelectUserProfileById))

    private fun <T> validateSession(validateSession: (UserSession, T) -> Try<T>): Task<TokenAndInput<T>, T, ApplicationContext> =
            Task.identity<TokenAndInput<T>, ApplicationContext>()
                    .query(SelectSessionByKey(validateSession))

    fun deleteAllUserActionChain(): Task<Unit, Unit, ApplicationContext> =
            Task.identity<Unit, ApplicationContext>()
                    .deleteAll("user_relationship_request")
                    .deleteAll("user_password")
                    .deleteAll("user_session")
                    .deleteAll("user_profile")
}