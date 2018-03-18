package playwright.skript.user

import org.funktionale.option.getOrElse
import org.funktionale.tries.Try
import playwright.skript.Skript
import playwright.skript.auth.AuthSkripts
import playwright.skript.common.ApplicationStage
import playwright.skript.ex.*
import playwright.skript.performer.PublishCommand
import playwright.skript.sql.transaction.SQLTransactionSkript
import playwright.skript.user.models.*
import playwright.skript.user.props.GetUserStageProps
import playwright.skript.user.sql.*
import java.time.Instant
import java.util.*

object UserSkripts {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }

    private val PUBLISH_USER_CREATE_EVENT: Skript<UserProfile, UserProfile, ApplicationStage<Unit>> =
            Skript.identity<UserProfile, ApplicationStage<Unit>>()
                    .serialize()
                    .publish { PublishCommand.Publish(userCreatedAddress, it) }
                    .deserialize(UserProfile::class.java)

    private val PUBLISH_USER_LOGIN_EVENT: Skript<UserSession, UserSession, ApplicationStage<Unit>> =
            Skript.identity<UserSession, ApplicationStage<Unit>>()
                    .serialize()
                    .publish { PublishCommand.Publish(userLoginAddress, it) }
                    .deserialize(UserSession::class.java)

    private val AUTHORIZE_USER: Skript<String, String, ApplicationStage<GetUserStageProps>> =
            Skript.mapTryWithStage { userId, stage ->
                stage.getStageProps().getUserSession()
                        .filter { it.userId == userId }
                        .map { Try.Success(userId) }
                        .getOrElse { Try.Failure<String>(UserError.AuthorizationFailed) }
    }

    val CREATE_USER_SKRIPT: Skript<UserProfileAndPassword, UserProfile, ApplicationStage<Unit>> =
            SQLTransactionSkript.transaction( Skript.identity<UserProfileAndPassword, ApplicationStage<Unit>>()
                    .update(InsertUserProfileMapping)
                    .update(InsertUserPasswordMapping)
                    .andThen(PUBLISH_USER_CREATE_EVENT))

    val LOGIN_USER_SKRIPT: Skript<UserNameAndPassword, UserSession, ApplicationStage<Unit>> =
            SQLTransactionSkript.transaction(Skript.identity<UserNameAndPassword, ApplicationStage<Unit>>()
                    .query(SelectUserIdForLogin)
                    .query(ValidatePasswordForUserId)
                    .query(EnsureNoSessionExists)
                    .map(createNewSessionKey)
                    .update(InsertSession)
                    .andThen(PUBLISH_USER_LOGIN_EVENT))


    val GET_USER_SKRIPT: Skript<String, UserProfile, ApplicationStage<GetUserStageProps>> =
            SQLTransactionSkript.autoCommit( AuthSkripts.validate<String, GetUserStageProps>()
                    .andThen(AUTHORIZE_USER)
                    .query(SelectUserProfileById)
            )

    val deleteAllUserActionChain = Skript.identity<Unit, ApplicationStage<Unit>>()
            .deleteAll("user_relationship_request")
            .deleteAll("user_password")
            .deleteAll("user_session")
            .deleteAll("user_profile")
}