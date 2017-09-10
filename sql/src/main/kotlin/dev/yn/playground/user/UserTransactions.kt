package dev.yn.playground.user

import dev.yn.playground.sql.SQLTransaction

object UserTransactions {
    val createUserTransaction = SQLTransaction.update(InsertUserProfileMapping)
            .update(InsertUserPasswordMapping)

    val authenticateUserTransaction: SQLTransaction<UserNameAndPassword, UserIdAndPassword, String> =
            SQLTransaction.query(SelectUserIdMapping)
                    .query(SelectUserByPassword)

}
