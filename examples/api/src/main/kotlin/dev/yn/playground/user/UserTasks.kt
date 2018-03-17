package dev.yn.playground.user

import dev.yn.playground.Skript
import dev.yn.playground.auth.TokenAndInput
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.ex.*
import dev.yn.playground.publisher.PublishCommand
import dev.yn.playground.user.models.*
import dev.yn.playground.user.sql.*
import dev.yn.playground.sql.transaction.SQLTransactionSkript
import org.funktionale.tries.Try
import java.time.Instant
import java.util.*

object UserSkripts {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }

    private val PUBLISH_USER_CREATE_EVENT: Skript<UserProfile, UserProfile, ApplicationContext> =
            Skript.identity<UserProfile, ApplicationContext>()
                    .serialize()
                    .publish { PublishCommand.Publish(userCreatedAddress, it) }
                    .deserialize(UserProfile::class.java)

    private val PUBLISH_USER_LOGIN_EVENT: Skript<UserSession, UserSession, ApplicationContext> =
            Skript.identity<UserSession, ApplicationContext>()
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

    val UNPREPARED_CREATE_SKRIPT: Skript<UserProfileAndPassword, UserProfile, ApplicationContext> =
            SQLTransactionSkript.transaction(
                    Skript.identity<UserProfileAndPassword, ApplicationContext>()
                            .update(InsertUserProfileMapping)
                            .update(InsertUserPasswordMapping)
                            .andThen(PUBLISH_USER_CREATE_EVENT))

    val UNPREPARED_LOGIN_SKRIPT: Skript<UserNameAndPassword, UserSession, ApplicationContext> =
            SQLTransactionSkript.transaction(
                    Skript.identity<UserNameAndPassword, ApplicationContext>()
                            .query(SelectUserIdForLogin)
                            .query(ValidatePasswordForUserId)
                            .query(EnsureNoSessionExists)
                            .map(createNewSessionKey)
                            .update(InsertSession)
                            .andThen(PUBLISH_USER_LOGIN_EVENT))


    val UNPREPARED_GET_SKRIPT: Skript<TokenAndInput<String>, UserProfile, ApplicationContext> =
            SQLTransactionSkript.autoCommit(
                    validateSession<String>(onlyIfRequestedUserMatchesSessionUser)
                            .query(SelectUserProfileById))

    private fun <T> validateSession(validateSession: (UserSession, T) -> Try<T>): Skript<TokenAndInput<T>, T, ApplicationContext> =
            Skript.identity<TokenAndInput<T>, ApplicationContext>()
                    .query(SelectSessionByKey(validateSession))

    fun deleteAllUserActionChain(): Skript<Unit, Unit, ApplicationContext> =
            Skript.identity<Unit, ApplicationContext>()
                    .deleteAll("user_relationship_request")
                    .deleteAll("user_password")
                    .deleteAll("user_session")
                    .deleteAll("user_profile")
}