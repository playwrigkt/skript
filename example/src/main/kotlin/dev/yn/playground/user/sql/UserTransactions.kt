package dev.yn.playground.user.sql

import dev.yn.playground.auth.TokenAndInput
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.publisher.PublishCommand
import dev.yn.playground.publisher.PublishTask
import dev.yn.playground.sql.SQLTask
import dev.yn.playground.sql.ext.deleteAll
import dev.yn.playground.sql.ext.query
import dev.yn.playground.sql.ext.update
import dev.yn.playground.task.Task
import dev.yn.playground.user.models.*
import org.funktionale.tries.Try
import java.time.Instant
import java.util.*
import dev.yn.playground.publisher.ex.*
import dev.yn.playground.user.userCreatedAddress
import dev.yn.playground.user.userLoginAddress
import io.vertx.core.json.JsonObject

object UserTransactions {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }
    private val publishUserCreateEvent: (UserProfile) -> PublishCommand.Publish =
            { PublishCommand.Publish(userCreatedAddress, JsonObject.mapFrom(it).encode().toByteArray())}
    private val publishUserLoginEvent: (UserSession) -> PublishCommand.Publish =
            { PublishCommand.Publish(userLoginAddress, JsonObject.mapFrom(it).encode().toByteArray())}

    val createUserActionChain: Task<UserProfileAndPassword, UserProfile, ApplicationContext> =
            SQLTask.update<UserProfileAndPassword, UserProfileAndPassword, ApplicationContext>(InsertUserProfileMapping)
                    .update(InsertUserPasswordMapping)
                    .publish(publishUserCreateEvent)

    val loginActionChain: Task<UserNameAndPassword, UserSession, ApplicationContext> =
            SQLTask.query<UserNameAndPassword, UserIdAndPassword, ApplicationContext>(SelectUserIdForLogin)
                    .query(ValidatePasswordForUserId)
                    .query(EnsureNoSessionExists)
                    .map(createNewSessionKey)
                    .update(InsertSession)
                    .publish(publishUserLoginEvent)

    private val onlyIfRequestedUserMatchesSessionUser =  { session: UserSession, userId: String ->
        if (session.userId == userId) {
            Try.Success(userId)
        } else {
            Try.Failure<String>(UserError.AuthorizationFailed)
        }
    }

    val getUserActionChain: Task<TokenAndInput<String>, UserProfile, ApplicationContext> =
            validateSession<String>(onlyIfRequestedUserMatchesSessionUser)
                    .query(SelectUserProfileById)

    private fun <T> validateSession(validateSession: (UserSession, T) -> Try<T>): Task<TokenAndInput<T>, T, ApplicationContext> =
            SQLTask.query(SelectSessionByKey(validateSession))

    fun deleteAllUserActionChain(): Task<Unit, Unit, ApplicationContext> =
            deleteAll<ApplicationContext>("user_relationship_request")
                    .deleteAll("user_password")
                    .deleteAll("user_session")
                    .deleteAll("user_profile")


}