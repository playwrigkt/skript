package playwrigkt.skript.sql

import io.kotlintest.specs.StringSpec

class SqlSkriptSpec : StringSpec() {
//    data class UserSession(val sessionKey: String, val userId: String, val expiration: Instant)
//
//
//
//    data class ApplicationTroupe<R>(val sqlPerformer: SqlPerformer, val cache: R):
//            SqlTroupe,
//            OperationCache<R> {
//        override fun getOperationCache(): R = cache
//
//        override fun getSQLPerformer():AsyncResult<SqlPerformer> {
//            return AsyncResult.succeeded(sqlPerformer)
//        }
//    }
//
//    interface OperationCache<R> {
//        fun getOperationCache(): R
//    }
//
//    interface UserSessionTroupe {
//        fun getUserSessionKey(): String
//        fun setUserSession(userSession: UserSession)
//        fun getUserSession(): Option<UserSession>
//    }
//
//    interface ExistingUserProfileTroupe {
//        fun getExistingProfile(): Option<UserProfile>
//        fun useProfile(profile: UserProfile)
//    }
//
//    data class UpdateUserProfileTroupe(val sessionKey: String,
//                                       var session: Option<UserSession> = Option.None,
//                                       var existing: Option<UserProfile> = Option.None): UserSessionTroupe, ExistingUserProfileTroupe {
//        override fun getUserSessionKey(): String = this.sessionKey
//
//        override fun setUserSession(userSession: UserSession) {
//            this.session = Option.Some(userSession)
//        }
//
//        override fun getUserSession(): Option<UserSession> = this.session
//
//        override fun getExistingProfile(): Option<UserProfile> = this.existing
//
//        override fun useProfile(profile: UserProfile) {
//            this.existing = Option.Some(profile)
//        }
//    }
//
//
//    data class UserProfile(val id: String, val name: String, val allowPublicMessage: Boolean)
//
//    object SelectUserProfileById: SqlQueryMapping<String, UserProfile> {
//        val selectUser = "SELECT id, user_name, allow_public_message FROM user_profile where id = ?"
//        override fun toSql(i: String): SqlCommand.Query = SqlCommand.Query(SqlStatement.Parameterized(selectUser, listOf(i)))
//
//        override fun mapResult(i: String, rs: SqlResult.Query): Try<UserProfile> =
//                Try { rs.result.next() }
//                        .rescue { Try.Failure(IllegalArgumentException("no such user")) }
//                        .map {
//                            UserProfile(it.getString("id"),
//                                    it.getString("user_name"),
//                                    it.getBoolean("allow_public_message")) }
//
//    }
//
//    object UpdateUserProfile: SqlUpdateMapping<UserProfile, UserProfile> {
//        override fun toSql(i: UserProfile): SqlCommand.Update {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }
//
//        override fun mapResult(i: UserProfile, rs: SqlResult.Update): Try<UserProfile> {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }
//    }
//
//    object SelectSessionByKey: SqlQueryMapping<String, UserSession> {
//        override fun toSql(i: String): SqlCommand.Query {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }
//
//        override fun mapResult(i: String, rs: SqlResult.Query): Try<UserSession> {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }
//    }
//
//    val onlyIfSessionUserIdMathcesRequested = { update: UserProfile, stage: ApplicationTroupe<UpdateUserProfileTroupe> ->
//        stage.getOperationCache().getUserSession()
//                .map { it.userId }
//                .filter { update.id == it }
//                .map { Try.Success(update) }
//                .getOrElse { Try.Failure<UserProfile>(IllegalArgumentException("not authorized")) }
//    }
//
//    val authenticateUpdateUserProfile = Skript.mapTryWithTroupe(onlyIfSessionUserIdMathcesRequested)
//
//    fun <R: ExistingUserProfileTroupe> addExistingUserToTroupe() =
//            Skript.updateTroupe(Skript.identity<UserProfile, ApplicationTroupe<R>>()
//                    .map { it.id }
//                    .query(SelectUserProfileById)
//                    .mapWithTroupe { existing, stage ->
//                        stage.getOperationCache().useProfile(existing)
//                    })
//
//    fun <R: ExistingUserProfileTroupe> failIfProfileNotCached() = Skript.mapTryWithTroupe<UserProfile, UserProfile, ApplicationTroupe<R>> {
//        i, stage ->
//        stage.getOperationCache().getExistingProfile()
//                .map { Try.Success(i) }
//                .getOrElse { Try.Failure<UserProfile>(IllegalArgumentException("No such user")) }
//    }
//
//    fun <I, R: UserSessionTroupe> validateSession(): Skript<I, I, ApplicationTroupe<R>> =
//            Skript.updateTroupe(Skript.identity<I, ApplicationTroupe<R>>()
//                            .mapWithTroupe { _, c -> c.cache.getUserSessionKey()}
//                            .query(SelectSessionByKey)
//                            .mapWithTroupe { session, c -> c.cache.setUserSession(session) })
//
//
//    init {
//        "Do a thing" {
//            val performer = object: SqlPerformer() {
//                override fun <T> close(): (T) -> AsyncResult<T> {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                }
//
//                override fun <T> closeOnFailure(): (Throwable) -> AsyncResult<T> {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                }
//
//                override fun <T> commit(): (T) -> AsyncResult<T> {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                }
//
//                override fun <T> rollback(): (Throwable) -> AsyncResult<T> {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                }
//
//                override fun setAutoCommit(autoCommit: Boolean): AsyncResult<Unit> {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                }
//
//                override fun query(query: SqlCommand.Query): AsyncResult<SqlResult.Query> {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                }
//
//                override fun update(update: SqlCommand.Update): AsyncResult<SqlResult.Update> {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                }
//
//                override fun exec(exec: SqlCommand.Exec): AsyncResult<SqlResult.Void> {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                }
//
//            }
//            val sessionKey = "TEST_SESSION_KEY"
//            ApplicationTroupe<UpdateUserProfileTroupe>(performer, UpdateUserProfileTroupe(sessionKey))
//
//
//            SqlTransactionSkript.transaction(
//                        validateSession<UserProfile, UpdateUserProfileTroupe>()
//                            .andThen(authenticateUpdateUserProfile)
//                            .andThen(addExistingUserToTroupe<UpdateUserProfileTroupe>())
//                            .andThen(failIfProfileNotCached<UpdateUserProfileTroupe>())
//                            .update(UpdateUserProfile))
//
//
//        }
//    }
}