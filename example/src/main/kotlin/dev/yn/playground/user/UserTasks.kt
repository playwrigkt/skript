package dev.yn.playground.user

import dev.yn.playground.auth.TokenAndInput
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.sql.task.UnpreparedSQLTask
import dev.yn.playground.sql.task.UnpreparedTransactionalSQLTask
import dev.yn.playground.task.UnpreparedTask

object UserTasks {
    val unpreparedCreateTask: UnpreparedTask<UserProfileAndPassword, UserProfile, ApplicationContextProvider> =
            UnpreparedTransactionalSQLTask.create(UserTransactions.createUserActionChain)

    val unpreparedLoginTask: UnpreparedTask<UserNameAndPassword, UserSession, ApplicationContextProvider> =
            UnpreparedTransactionalSQLTask.create(UserTransactions.loginActionChain)

    val unpreparedGetTask: UnpreparedTask<TokenAndInput<String>, UserProfile, ApplicationContextProvider> =
            UnpreparedSQLTask.create(UserTransactions.getUserActionChain)
}