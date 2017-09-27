package dev.yn.playground.test

import dev.yn.playground.sql.task.SQLClientProvider
import dev.yn.playground.sql.task.UnpreparedSQLTask
import dev.yn.playground.sql.task.UnpreparedTransactionalSQLTask
import dev.yn.playground.task.UnpreparedTask
import dev.yn.playground.task.VertxProvider
import io.vertx.core.Vertx
import io.vertx.ext.sql.SQLClient

class TestSQLAndVertxProvider(val vertx: Vertx, val sqlClient: SQLClient) : SQLClientProvider, VertxProvider {
    override fun provideVertx(): Vertx = vertx

    override fun provideSQLClient(): SQLClient {
        return sqlClient
    }
}


object UserTasks {
    val unpreparedCreateTask: UnpreparedTask<UserProfileAndPassword, UserProfile, TestSQLAndVertxProvider> =
            UnpreparedTransactionalSQLTask.chain(UserTransactions.createUserActionChain<TestSQLAndVertxProvider>())

    val unpreparedLoginTask: UnpreparedTask<UserNameAndPassword, UserSession, TestSQLAndVertxProvider> =
            UnpreparedTransactionalSQLTask.chain(UserTransactions.loginActionChain<TestSQLAndVertxProvider>())

    val unpreparedGetTask: UnpreparedTask<TokenAndInput<String>, UserProfile, TestSQLAndVertxProvider> =
            UnpreparedSQLTask.chain(UserTransactions.getUserActionChain<TestSQLAndVertxProvider>())
}