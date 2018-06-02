package playwrigkt.skript.user

import arrow.core.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.*
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.sql.transaction.SqlTransactionSkript
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.models.*
import playwrigkt.skript.user.sql.*
import java.time.Instant
import java.util.*

object UserSkripts {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }

    val userCreatedAddress = "user.updated"
    val userLoginAddress = "user.login"

    private val PUBLISH_USER_CREATE_EVENT: Skript<UserProfile, UserProfile, ApplicationTroupe> =
            Skript.identity<UserProfile, ApplicationTroupe>()
                    .serialize()
                    .publish { QueueMessage(userCreatedAddress, it) }
                    .deserialize(UserProfile::class.java)

    private val PUBLISH_USER_LOGIN_EVENT: Skript<UserSession, UserSession, ApplicationTroupe> =
            Skript.identity<UserSession, ApplicationTroupe>()
                    .serialize()
                    .publish { QueueMessage(userLoginAddress, it) }
                    .deserialize(UserSession::class.java)

    private val onlyIfRequestedUserMatchesSessionUser =  { session: UserSession, userId: String ->
        if (session.userId == userId) {
            Try.Success(userId)
        } else {
            Try.Failure<String>(UserError.AuthorizationFailed)
        }
    }

    val createSkript: Skript<UserProfileAndPassword, UserProfile, ApplicationTroupe> =
            SqlTransactionSkript.transaction(
                    Skript.identity<UserProfileAndPassword, ApplicationTroupe>()
                            .update(InsertUserProfileMapping)
                            .update(InsertUserPasswordMapping)
                            .andThen(PUBLISH_USER_CREATE_EVENT))

    val loginSkript: Skript<UserNameAndPassword, UserSession, ApplicationTroupe> =
            SqlTransactionSkript.transaction(
                    Skript.identity<UserNameAndPassword, ApplicationTroupe>()
                            .query(SelectUserIdForLogin)
                            .query(ValidatePasswordForUserId)
                            .query(EnsureNoSessionExists)
                            .map(createNewSessionKey)
                            .update(InsertSession)
                            .andThen(PUBLISH_USER_LOGIN_EVENT))


    val getSkript: Skript<playwrigkt.skript.auth.TokenAndInput<String>, UserProfile, ApplicationTroupe> =
            SqlTransactionSkript.autoCommit(
                    validateSession(onlyIfRequestedUserMatchesSessionUser)
                            .query(SelectUserProfileById))

    private fun <T> validateSession(validateSession: (UserSession, T) -> Try<T>): Skript<playwrigkt.skript.auth.TokenAndInput<T>, T, ApplicationTroupe> =
            Skript.identity<playwrigkt.skript.auth.TokenAndInput<T>, ApplicationTroupe>()
                    .query(SelectSessionByKey(validateSession))

    fun deleteAllUserActionChain(): Skript<Unit, Unit, ApplicationTroupe> =
            Skript.identity<Unit, ApplicationTroupe>()
                    .deleteAll("user_relationship_request")
                    .deleteAll("user_password")
                    .deleteAll("user_session")
                    .deleteAll("user_profile")
}