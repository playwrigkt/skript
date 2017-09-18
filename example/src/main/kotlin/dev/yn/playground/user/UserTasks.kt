package dev.yn.playground.user

import dev.yn.playground.sql.task.UnpreparedSQLTask
import dev.yn.playground.sql.task.UnpreparedTransactionalSQLTask
import dev.yn.playground.task.UnpreparedTask

object UserTasks {
    val unpreparedCreateTask: UnpreparedTask<UserProfileAndPassword, UserProfile, SQLAndVertxProvider> =
            UnpreparedTransactionalSQLTask.chain(UserTransactions.createUserActionChain<SQLAndVertxProvider>())

    val unpreparedLoginTask: UnpreparedTask<UserNameAndPassword, UserSession, SQLAndVertxProvider> =
            UnpreparedTransactionalSQLTask.chain(UserTransactions.loginActionChain<SQLAndVertxProvider>())

    val unpreparedGetTask: UnpreparedTask<TokenAndInput<String>, UserProfile, SQLAndVertxProvider> =
            UnpreparedSQLTask.chain(UserTransactions.getUserActionChain<SQLAndVertxProvider>())
}