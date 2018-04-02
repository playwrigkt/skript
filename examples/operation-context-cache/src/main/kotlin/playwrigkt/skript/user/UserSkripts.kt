package playwrigkt.skript.user

import org.funktionale.option.getOrElse
import org.funktionale.tries.Try
import playwright.skript.queue.QueueMessage
import playwrigkt.skript.Skript
import playwrigkt.skript.auth.AuthSkripts
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.ex.*
import playwrigkt.skript.sql.transaction.SQLTransactionSkript
import playwrigkt.skript.user.models.*
import playwrigkt.skript.user.props.GetUserTroupeProps
import playwrigkt.skript.user.sql.*
import java.time.Instant
import java.util.*

object UserSkripts {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }

    private val PUBLISH_USER_CREATE_EVENT: Skript<UserProfile, UserProfile, ApplicationTroupe<Unit>> =
            Skript.identity<UserProfile, ApplicationTroupe<Unit>>()
                    .serialize()
                    .publish { QueueMessage(userCreatedAddress, it) }
                    .deserialize(UserProfile::class.java)

    private val PUBLISH_USER_LOGIN_EVENT: Skript<UserSession, UserSession, ApplicationTroupe<Unit>> =
            Skript.identity<UserSession, ApplicationTroupe<Unit>>()
                    .serialize()
                    .publish { QueueMessage(userLoginAddress, it) }
                    .deserialize(UserSession::class.java)

    private val AUTHORIZE_USER: Skript<String, String, ApplicationTroupe<GetUserTroupeProps>> =
            Skript.mapTryWithTroupe { userId, stage ->
                stage.getTroupeProps().getUserSession()
                        .filter { it.userId == userId }
                        .map { Try.Success(userId) }
                        .getOrElse { Try.Failure<String>(UserError.AuthorizationFailed) }
    }

    val CREATE_USER_SKRIPT: Skript<UserProfileAndPassword, UserProfile, ApplicationTroupe<Unit>> =
            SQLTransactionSkript.transaction( Skript.identity<UserProfileAndPassword, ApplicationTroupe<Unit>>()
                    .update(InsertUserProfileMapping)
                    .update(InsertUserPasswordMapping)
                    .andThen(PUBLISH_USER_CREATE_EVENT))

    val LOGIN_USER_SKRIPT: Skript<UserNameAndPassword, UserSession, ApplicationTroupe<Unit>> =
            SQLTransactionSkript.transaction(Skript.identity<UserNameAndPassword, ApplicationTroupe<Unit>>()
                    .query(SelectUserIdForLogin)
                    .query(ValidatePasswordForUserId)
                    .query(EnsureNoSessionExists)
                    .map(createNewSessionKey)
                    .update(InsertSession)
                    .andThen(PUBLISH_USER_LOGIN_EVENT))


    val GET_USER_SKRIPT: Skript<String, UserProfile, ApplicationTroupe<GetUserTroupeProps>> =
            SQLTransactionSkript.autoCommit( AuthSkripts.validate<String, GetUserTroupeProps>()
                    .andThen(AUTHORIZE_USER)
                    .query(SelectUserProfileById)
            )

    val deleteAllUserActionChain = Skript.identity<Unit, ApplicationTroupe<Unit>>()
            .deleteAll("user_relationship_request")
            .deleteAll("user_password")
            .deleteAll("user_session")
            .deleteAll("user_profile")
}