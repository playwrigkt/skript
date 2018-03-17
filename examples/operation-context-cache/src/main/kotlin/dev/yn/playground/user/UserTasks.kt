package dev.yn.playground.user

import dev.yn.playground.Task
import dev.yn.playground.auth.AuthTasks
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.ex.*
import dev.yn.playground.publisher.PublishCommand
import dev.yn.playground.user.context.GetUserContext
import dev.yn.playground.user.models.*
import dev.yn.playground.user.sql.*
import devyn.playground.sql.task.SQLTransactionTask
import org.funktionale.option.getOrElse
import org.funktionale.tries.Try
import java.time.Instant
import java.util.*

object UserTasks {
    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }

    private val publishUserCreateEvent: Task<UserProfile, UserProfile, ApplicationContext<Unit>> =
            Task.identity<UserProfile, ApplicationContext<Unit>>()
                    .serialize()
                    .publish { PublishCommand.Publish(userCreatedAddress, it) }
                    .deserialize(UserProfile::class.java)

    private val publishUserLoginEvent: Task<UserSession, UserSession, ApplicationContext<Unit>> =
            Task.identity<UserSession, ApplicationContext<Unit>>()
                    .serialize()
                    .publish { PublishCommand.Publish(userLoginAddress, it) }
                    .deserialize(UserSession::class.java)

    private val authorizeUser: Task<String, String, ApplicationContext<GetUserContext>> =
            Task.mapTryWithContext { userId, context ->
                context.cache.getUserSession()
                        .filter { it.userId == userId }
                        .map { Try.Success(userId) }
                        .getOrElse { Try.Failure<String>(UserError.AuthorizationFailed) }
    }

    val createUserTask: Task<UserProfileAndPassword, UserProfile, ApplicationContext<Unit>> =
            SQLTransactionTask.transaction( Task.identity<UserProfileAndPassword, ApplicationContext<Unit>>()
                    .update(InsertUserProfileMapping)
                    .update(InsertUserPasswordMapping)
                    .andThen(publishUserCreateEvent))

    val loginUserTask: Task<UserNameAndPassword, UserSession, ApplicationContext<Unit>> =
            SQLTransactionTask.transaction(Task.identity<UserNameAndPassword, ApplicationContext<Unit>>()
                    .query(SelectUserIdForLogin)
                    .query(ValidatePasswordForUserId)
                    .query(EnsureNoSessionExists)
                    .map(createNewSessionKey)
                    .update(InsertSession)
                    .andThen(publishUserLoginEvent))


    val getUserTask: Task<String, UserProfile, ApplicationContext<GetUserContext>> =
            SQLTransactionTask.autoCommit( AuthTasks.validate<String, GetUserContext>()
                    .andThen(authorizeUser)
                    .query(SelectUserProfileById)
            )

    val deleteAllUserActionChain = Task.identity<Unit, ApplicationContext<Unit>>()
            .deleteAll("user_relationship_request")
            .deleteAll("user_password")
            .deleteAll("user_session")
            .deleteAll("user_profile")
}