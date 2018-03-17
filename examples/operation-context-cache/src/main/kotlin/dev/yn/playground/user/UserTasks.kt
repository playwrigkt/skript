package dev.yn.playground.user

import dev.yn.playground.Skript
import dev.yn.playground.auth.AuthTasks
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.ex.*
import dev.yn.playground.publisher.PublishCommand
import dev.yn.playground.user.context.GetUserContext
import dev.yn.playground.user.models.*
import dev.yn.playground.user.sql.*
import dev.yn.playground.sql.transaction.SQLTransactionSkript
import org.funktionale.option.getOrElse
import org.funktionale.tries.Try
import java.time.Instant
import java.util.*

object UserTasks {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }

    private val PUBLISH_USER_CREATE_EVENT: Skript<UserProfile, UserProfile, ApplicationContext<Unit>> =
            Skript.identity<UserProfile, ApplicationContext<Unit>>()
                    .serialize()
                    .publish { PublishCommand.Publish(userCreatedAddress, it) }
                    .deserialize(UserProfile::class.java)

    private val PUBLISH_USER_LOGIN_EVENT: Skript<UserSession, UserSession, ApplicationContext<Unit>> =
            Skript.identity<UserSession, ApplicationContext<Unit>>()
                    .serialize()
                    .publish { PublishCommand.Publish(userLoginAddress, it) }
                    .deserialize(UserSession::class.java)

    private val AUTHORIZE_USER: Skript<String, String, ApplicationContext<GetUserContext>> =
            Skript.mapTryWithContext { userId, context ->
                context.cache.getUserSession()
                        .filter { it.userId == userId }
                        .map { Try.Success(userId) }
                        .getOrElse { Try.Failure<String>(UserError.AuthorizationFailed) }
    }

    val CREATE_USER_SKRIPT: Skript<UserProfileAndPassword, UserProfile, ApplicationContext<Unit>> =
            SQLTransactionSkript.transaction( Skript.identity<UserProfileAndPassword, ApplicationContext<Unit>>()
                    .update(InsertUserProfileMapping)
                    .update(InsertUserPasswordMapping)
                    .andThen(PUBLISH_USER_CREATE_EVENT))

    val LOGIN_USER_SKRIPT: Skript<UserNameAndPassword, UserSession, ApplicationContext<Unit>> =
            SQLTransactionSkript.transaction(Skript.identity<UserNameAndPassword, ApplicationContext<Unit>>()
                    .query(SelectUserIdForLogin)
                    .query(ValidatePasswordForUserId)
                    .query(EnsureNoSessionExists)
                    .map(createNewSessionKey)
                    .update(InsertSession)
                    .andThen(PUBLISH_USER_LOGIN_EVENT))


    val GET_USER_SKRIPT: Skript<String, UserProfile, ApplicationContext<GetUserContext>> =
            SQLTransactionSkript.autoCommit( AuthTasks.validate<String, GetUserContext>()
                    .andThen(AUTHORIZE_USER)
                    .query(SelectUserProfileById)
            )

    val deleteAllUserActionChain = Skript.identity<Unit, ApplicationContext<Unit>>()
            .deleteAll("user_relationship_request")
            .deleteAll("user_password")
            .deleteAll("user_session")
            .deleteAll("user_profile")
}