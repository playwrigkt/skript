package dev.yn.playground.user

import dev.yn.playground.sql.SQLTransaction
import dev.yn.playground.sql.extensions.transaction.*

object UserTransactions {
    val createUserTransaction = update(InsertUserProfileMapping)
            .update(InsertUserPasswordMapping)

    val authenticateUserTransaction: SQLTransaction<UserNameAndPassword, UserIdAndPassword, String> =
            query(SelectUserIdMapping)
                    .query(SelectUserByPassword)

    val deleteAllUsersTransaction: SQLTransaction<Unit, Unit, Unit> = deleteAll("user_password")
            .deleteAll {"user_profile"}
}
