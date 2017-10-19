package dev.yn.playground.user

import dev.yn.playground.auth.TokenAndInput
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.sql.task.UnpreparedSQLTask
import dev.yn.playground.sql.task.UnpreparedTransactionalSQLTask
import dev.yn.playground.task.UnpreparedTask
import dev.yn.playground.user.models.UserNameAndPassword
import dev.yn.playground.user.models.UserProfile
import dev.yn.playground.user.models.UserProfileAndPassword
import dev.yn.playground.user.models.UserSession
import dev.yn.playground.user.sql.UserTransactions

object UserTasks {
    val unpreparedCreateTask: UnpreparedTask<UserProfileAndPassword, UserProfile, ApplicationContextProvider> =
            UnpreparedTransactionalSQLTask.create(UserTransactions.createUserActionChain)

    val unpreparedLoginTask: UnpreparedTask<UserNameAndPassword, UserSession, ApplicationContextProvider> =
            UnpreparedTransactionalSQLTask.create(UserTransactions.loginActionChain)

    val unpreparedGetTask: UnpreparedTask<TokenAndInput<String>, UserProfile, ApplicationContextProvider> =
            UnpreparedSQLTask.create(UserTransactions.getUserActionChain)
}