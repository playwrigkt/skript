package dev.yn.playground.test

import dev.yn.playground.sql.*
import org.funktionale.tries.Try
import java.time.Instant
import java.util.*

val userCreatedAddress = "user.created.vertx"
val userLoginAddress = "user.login.vertx"

//object UserTransactions {
//    private val createNewSessionKey: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }
//
//    fun createUserActionChain(): SQLTask<UserProfileAndPassword, UserProfile> =
//            SQLTask.update<UserProfileAndPassword, UserProfileAndPassword>(InsertUserProfileMapping)
//                    .update(InsertUserPasswordMapping)
////                    .mapTask<UserProfile>(VertxTask.sendWithResponse(userCreatedAddress))
//
//    fun loginActionChain(): SQLTask<UserNameAndPassword, UserSession> =
//            SQLTask.query<UserNameAndPassword, UserIdAndPassword>(SelectUserIdForLogin)
//                    .query(ValidatePasswordForUserId)
//                    .query(EnsureNoSessionExists)
//                    .map(createNewSessionKey)
//                    .update(InsertSession)
////                    .mapTask<UserSession>(VertxTask.sendWithResponse(userLoginAddress))
//
//    fun getUserActionChain(): SQLTask<TokenAndInput<String>, UserProfile> =
//            validateSession<String> { session, userId ->
//                if (session.userId == userId) {
//                    Try.Success(userId)
//                } else {
//                    Try.Failure(UserError.AuthorizationFailed)
//                }
//            }
//                    .query(SelectUserProfileById)
//
//    private fun <T> validateSession(validateSession: (UserSession, T) -> Try<T>): SQLTask<TokenAndInput<T>, T> =
//            SQLTask.query(SelectSessionByKey(validateSession))
//
//    fun deleteAllUserActionChain(): SQLTask<Unit, Unit> =
//            SQLTask.exec(deleteMapping("user_relationship_request"))
//                    .exec(deleteMapping("user_password"))
//                    .exec(deleteMapping("user_session"))
//                    .exec(deleteMapping("user_provile"))
//
//    fun deleteMapping(tableName: String) : SQLMapping<Unit, Unit, SQLCommand.Exec, SQLResult.Void> {
//        return SQLMapping.exec(
//                { SQLCommand.Exec(SQLStatement.Simple("user_relationship_request")) },
//                { i: Unit, exec -> Try.Success(Unit) }
//        )
//    }
//}