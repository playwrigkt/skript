package playwrigkt.skript.sql

import io.kotlintest.mock.mock
import io.kotlintest.specs.StringSpec
import org.funktionale.option.Option
import org.funktionale.option.getOrElse
import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.ex.query
import playwrigkt.skript.ex.update
import playwrigkt.skript.performer.SQLPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.sql.transaction.SQLTransactionSkript
import playwrigkt.skript.troupe.SQLTroupe
import java.time.Instant

class SQLSkriptSpec : StringSpec() {
    data class UserSession(val sessionKey: String, val userId: String, val expiration: Instant)



    data class ApplicationTroupe<R>(val sqlPerformer: SQLPerformer, val cache: R):
            SQLTroupe,
            OperationCache<R> {
        override fun getOperationCache(): R = cache

        override fun getSQLPerformer():AsyncResult<SQLPerformer> {
            return AsyncResult.succeeded(sqlPerformer)
        }
    }

    interface OperationCache<R> {
        fun getOperationCache(): R
    }

    interface UserSessionTroupe {
        fun getUserSessionKey(): String
        fun setUserSession(userSession: UserSession)
        fun getUserSession(): Option<UserSession>
    }

    interface ExistingUserProfileTroupe {
        fun getExistingProfile(): Option<UserProfile>
        fun useProfile(profile: UserProfile)
    }

    data class UpdateUserProfileTroupe(val sessionKey: String,
                                       var session: Option<UserSession> = Option.None,
                                       var existing: Option<UserProfile> = Option.None): UserSessionTroupe, ExistingUserProfileTroupe {
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

    val onlyIfSessionUserIdMathcesRequested = { update: UserProfile, stage: ApplicationTroupe<UpdateUserProfileTroupe> ->
        stage.getOperationCache().getUserSession()
                .map { it.userId }
                .filter { update.id == it }
                .map { Try.Success(update) }
                .getOrElse { Try.Failure<UserProfile>(IllegalArgumentException("not authorized")) }
    }

    val authenticateUpdateUserProfile = Skript.mapTryWithTroupe(onlyIfSessionUserIdMathcesRequested)

    fun <R: ExistingUserProfileTroupe> addExistingUserToTroupe() =
            Skript.updateTroupe(Skript.identity<UserProfile, ApplicationTroupe<R>>()
                    .map { it.id }
                    .query(SelectUserProfileById)
                    .mapWithTroupe { existing, stage ->
                        stage.getOperationCache().useProfile(existing)
                    })

    fun <R: ExistingUserProfileTroupe> failIfProfileNotCached() = Skript.mapTryWithTroupe<UserProfile, UserProfile, ApplicationTroupe<R>> {
        i, stage ->
        stage.getOperationCache().getExistingProfile()
                .map { Try.Success(i) }
                .getOrElse { Try.Failure<UserProfile>(IllegalArgumentException("No such user")) }
    }

    fun <I, R: UserSessionTroupe> validateSession(): Skript<I, I, ApplicationTroupe<R>> =
            Skript.updateTroupe(Skript.identity<I, ApplicationTroupe<R>>()
                            .mapWithTroupe { _, c -> c.cache.getUserSessionKey()}
                            .query(SelectSessionByKey)
                            .mapWithTroupe { session, c -> c.cache.setUserSession(session) })


    init {
        "Do a thing" {
            val performer = mock<SQLPerformer>()
            val sessionKey = "TEST_SESSION_KEY"
            ApplicationTroupe<UpdateUserProfileTroupe>(performer, UpdateUserProfileTroupe(sessionKey))


            SQLTransactionSkript.transaction(
                        validateSession<UserProfile, UpdateUserProfileTroupe>()
                            .andThen(authenticateUpdateUserProfile)
                            .andThen(addExistingUserToTroupe<UpdateUserProfileTroupe>())
                            .andThen(failIfProfileNotCached<UpdateUserProfileTroupe>())
                            .update(UpdateUserProfile))


        }
    }
}