package playwright.skript.sql

import io.kotlintest.mock.mock
import io.kotlintest.specs.StringSpec
import org.funktionale.option.Option
import org.funktionale.option.getOrElse
import org.funktionale.tries.Try
import playwright.skript.Skript
import playwright.skript.ex.andThen
import playwright.skript.ex.query
import playwright.skript.ex.update
import playwright.skript.performer.SQLPerformer
import playwright.skript.sql.transaction.SQLTransactionSkript
import playwright.skript.stage.SQLStage
import java.time.Instant

class SQLSkriptSpec : StringSpec() {
    data class UserSession(val sessionKey: String, val userId: String, val expiration: Instant)



    data class ApplicationStage<R>(val sqlPerformer: SQLPerformer, val cache: R):
            SQLStage<SQLPerformer>,
            OperationCache<R> {
        override fun getOperationCache(): R = cache

        override fun getSQLPerformer(): SQLPerformer {
            return sqlPerformer
        }
    }

    interface OperationCache<R> {
        fun getOperationCache(): R
    }

    interface UserSessionContext {
        fun getUserSessionKey(): String
        fun setUserSession(userSession: UserSession)
        fun getUserSession(): Option<UserSession>
    }

    interface ExistingUserProfileContext {
        fun getExistingProfile(): Option<UserProfile>
        fun useProfile(profile: UserProfile)
    }

    data class UpdateUserProfileContext(val sessionKey: String,
                                        var session: Option<UserSession> = Option.None,
                                        var existing: Option<UserProfile> = Option.None): UserSessionContext, ExistingUserProfileContext {
        override fun getUserSessionKey(): String = this.sessionKey

        override fun setUserSession(userSession: UserSession) {
            this.session = Option.Some(userSession)
        }

        override fun getUserSession(): Option<UserSession> = this.session

        override fun getExistingProfile(): Option<UserProfile> = this.existing

        override fun useProfile(profile: UserProfile) {
            this.existing = Option.Some(profile)
        }
    }


    data class UserProfile(val id: String, val name: String, val allowPublicMessage: Boolean)

    object SelectUserProfileById: SQLQueryMapping<String, UserProfile> {
        val selectUser = "SELECT id, user_name, allow_public_message FROM user_profile where id = ?"
        override fun toSql(i: String): SQLCommand.Query = SQLCommand.Query(SQLStatement.Parameterized(selectUser, listOf(i)))

        override fun mapResult(i: String, rs: SQLResult.Query): Try<UserProfile> =
                Try { rs.result.next() }
                        .rescue { Try.Failure(IllegalArgumentException("no such user")) }
                        .map {
                            UserProfile(it.getString("id"),
                                    it.getString("user_name"),
                                    it.getBoolean("allow_public_message")) }

    }

    object UpdateUserProfile: SQLUpdateMapping<UserProfile, UserProfile> {
        override fun toSql(i: UserProfile): SQLCommand.Update {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun mapResult(i: UserProfile, rs: SQLResult.Update): Try<UserProfile> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    object SelectSessionByKey: SQLQueryMapping<String, UserSession> {
        override fun toSql(i: String): SQLCommand.Query {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun mapResult(i: String, rs: SQLResult.Query): Try<UserSession> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    val onlyIfSessionUserIdMathcesRequested = { update: UserProfile, context: ApplicationStage<UpdateUserProfileContext> ->
        context.getOperationCache().getUserSession()
                .map { it.userId }
                .filter { update.id == it }
                .map { Try.Success(update) }
                .getOrElse { Try.Failure<UserProfile>(IllegalArgumentException("not authorized")) }
    }

    val authenticateUpdateUserProfile = Skript.mapTryWithContext(onlyIfSessionUserIdMathcesRequested)

    fun <R: ExistingUserProfileContext> addExistingUserToContext() =
            Skript.updateContext(Skript.identity<UserProfile, ApplicationStage<R>>()
                    .map { it.id }
                    .query(SelectUserProfileById)
                    .mapWithContext { existing, context ->
                        context.getOperationCache().useProfile(existing)
                    })

    fun <R: ExistingUserProfileContext> failIfProfileNotCached() = Skript.mapTryWithContext<UserProfile, UserProfile, ApplicationStage<R>> {
        i, context ->
        context.getOperationCache().getExistingProfile()
                .map { Try.Success(i) }
                .getOrElse { Try.Failure<UserProfile>(IllegalArgumentException("No such user")) }
    }

    fun <I, R: UserSessionContext> validateSession(): Skript<I, I, ApplicationStage<R>> =
            Skript.updateContext(Skript.identity<I, ApplicationStage<R>>()
                            .mapWithContext { _, c -> c.cache.getUserSessionKey()}
                            .query(SelectSessionByKey)
                            .mapWithContext { session, c -> c.cache.setUserSession(session) })


    init {
        "Do a thing" {
            val performer = mock<SQLPerformer>()
            val sessionKey = "TEST_SESSION_KEY"
            val context = ApplicationStage<UpdateUserProfileContext>(performer, UpdateUserProfileContext(sessionKey))

            val udpateUser: SQLTransactionSkript<UserProfile, UserProfile, ApplicationStage<UpdateUserProfileContext>> =
                    SQLTransactionSkript.transaction(
                                validateSession<UserProfile, UpdateUserProfileContext>()
                                    .andThen(authenticateUpdateUserProfile)
                                    .andThen(addExistingUserToContext<UpdateUserProfileContext>())
                                    .andThen(failIfProfileNotCached<UpdateUserProfileContext>())
                                    .update(UpdateUserProfile))


        }
    }
}