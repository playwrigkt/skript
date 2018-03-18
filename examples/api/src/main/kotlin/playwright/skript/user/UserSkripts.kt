package playwright.skript.user

import org.funktionale.tries.Try
import playwright.skript.Skript
import playwright.skript.common.ApplicationStage
import playwright.skript.ex.*
import playwright.skript.performer.PublishCommand
import playwright.skript.sql.transaction.SQLTransactionSkript
import playwright.skript.user.models.*
import playwright.skript.user.sql.*
import java.time.Instant
import java.util.*

object UserSkripts {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }

    private val PUBLISH_USER_CREATE_EVENT: Skript<UserProfile, UserProfile, ApplicationStage> =
            Skript.identity<UserProfile, ApplicationStage>()
                    .serialize()
                    .publish { PublishCommand.Publish(userCreatedAddress, it) }
                    .deserialize(UserProfile::class.java)

    private val PUBLISH_USER_LOGIN_EVENT: Skript<UserSession, UserSession, ApplicationStage> =
            Skript.identity<UserSession, ApplicationStage>()
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

    val UNPREPARED_CREATE_SKRIPT: Skript<UserProfileAndPassword, UserProfile, ApplicationStage> =
            SQLTransactionSkript.transaction(
                    Skript.identity<UserProfileAndPassword, ApplicationStage>()
                            .update(InsertUserProfileMapping)
                            .update(InsertUserPasswordMapping)
                            .andThen(PUBLISH_USER_CREATE_EVENT))

    val UNPREPARED_LOGIN_SKRIPT: Skript<UserNameAndPassword, UserSession, ApplicationStage> =
            SQLTransactionSkript.transaction(
                    Skript.identity<UserNameAndPassword, ApplicationStage>()
                            .query(SelectUserIdForLogin)
                            .query(ValidatePasswordForUserId)
                            .query(EnsureNoSessionExists)
                            .map(createNewSessionKey)
                            .update(InsertSession)
                            .andThen(PUBLISH_USER_LOGIN_EVENT))


    val UNPREPARED_GET_SKRIPT: Skript<playwright.skript.auth.TokenAndInput<String>, UserProfile, ApplicationStage> =
            SQLTransactionSkript.autoCommit(
                    validateSession<String>(onlyIfRequestedUserMatchesSessionUser)
                            .query(SelectUserProfileById))

    private fun <T> validateSession(validateSession: (UserSession, T) -> Try<T>): Skript<playwright.skript.auth.TokenAndInput<T>, T, ApplicationStage> =
            Skript.identity<playwright.skript.auth.TokenAndInput<T>, ApplicationStage>()
                    .query(SelectSessionByKey(validateSession))

    fun deleteAllUserActionChain(): Skript<Unit, Unit, ApplicationStage> =
            Skript.identity<Unit, ApplicationStage>()
                    .deleteAll("user_relationship_request")
                    .deleteAll("user_password")
                    .deleteAll("user_session")
                    .deleteAll("user_profile")
}