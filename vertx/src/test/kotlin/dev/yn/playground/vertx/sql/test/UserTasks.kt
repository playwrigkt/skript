package dev.yn.playground.test

import dev.yn.playground.sql.context.SQLTaskContextProvider
import dev.yn.playground.task.result.AsyncResult
import dev.yn.playground.vertx.sql.VertxSQLExecutor
import dev.yn.playground.vertx.task.*
import io.vertx.core.Future
import io.vertx.core.Vertx

class TestVertxSQLAndVertxProvider(val vertx: Vertx, val sqlConnectionContext: VertxSQLExecutor) : VertxProvider, SQLTaskContextProvider<VertxSQLExecutor> {
    override fun getConnection(): AsyncResult<VertxSQLExecutor> {
        val future = Future.future<VertxSQLExecutor>()
        future.complete(sqlConnectionContext)
        return VertxResult(future)
    }

    override fun provideVertx(): AsyncResult<Vertx> = AsyncResult.succeeded(vertx)
}


object UserTasks {
//    val unpreparedCreateTask: UnpreparedTask<UserProfileAndPassword, UserProfile, TestVertxSQLAndVertxProvider> =
//            UnpreparedTransactionalVertxSQLTask.create(UserTransactions.createUserActionChain())
//
//    val unpreparedLoginTask: UnpreparedTask<UserNameAndPassword, UserSession, TestVertxSQLAndVertxProvider> =
//            UnpreparedTransactionalVertxSQLTask.create(UserTransactions.loginActionChain())
//
//    val unpreparedGetTask: UnpreparedTask<TokenAndInput<String>, UserProfile, TestVertxSQLAndVertxProvider> =
//            UnpreparedVertxSQLTask.create(UserTransactions.getUserActionChain())
}