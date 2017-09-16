package dev.yn.playground.user.quijibo

/**
 * Created by devyn on 9/15/17.
 */
import dev.yn.playground.sql.SQLTransaction
import dev.yn.playground.sql.task.SQLClientProvider
import dev.yn.playground.sql.task.SQLTask
import dev.yn.playground.task.UnpreparedTask
import dev.yn.playground.task.VertxProvider
import dev.yn.playground.task.VertxTask
import dev.yn.playground.task.vertxAsync
import dev.yn.playground.user.*
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.sql.SQLClient
import java.time.Instant
import java.util.*

val userLoginAddress = "user.login"

val loginTransaction: SQLTransaction<UserNameAndPassword, UserSession> =
        SQLTransaction.query(SelectUserIdForLogin)
                .query(ValidatePasswordForUserId)
                .query(EnsureNoSessionExists)
                .map { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }
                .update(InsertSession)

val unpreparedLoginTask: UnpreparedTask<UserNameAndPassword, UserSession, SQLAndVertxProvider> =
        SQLTask.unpreparedTransactionalSql<UserNameAndPassword, UserSession, SQLAndVertxProvider>(loginTransaction)
                .vertxAsync(VertxTask.sendAndForget(userLoginAddress))

class SQLAndVertxProvider(val vertx: Vertx, val sqlClient: SQLClient) : SQLClientProvider, VertxProvider {
    override fun provideVertx(): Vertx = vertx

    override fun provideSQLClient(): SQLClient{
        return sqlClient
    }
}

class LoginService(val vertx: Vertx, val client: SQLClient) {
    val provider = SQLAndVertxProvider(vertx, client)
    val loginTask = unpreparedLoginTask.prepare(provider)

    fun login(userNameAndPassword: UserNameAndPassword): Future<UserSession> = loginTask.run(userNameAndPassword)
}