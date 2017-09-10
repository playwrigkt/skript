package dev.yn.playground.user

import dev.yn.playground.sql.SQLTransaction
import io.vertx.core.Future
import org.funktionale.either.Either
import org.funktionale.either.getOrElse

object UserTransactions {
    val createUserTransaction = SQLTransaction.update(InsertUserProfileMapping)
            .update(InsertUserPasswordMapping)

    val authenticateUserTransaction: SQLTransaction<UserNameAndPassword, Either<UserError, UserIdAndPassword>, String> =
            SQLTransaction.query(SelectUserIdMapping)
                    .flatMap(this::failLeft)
                    .query(SelectUserByPassword)
                    .flatMap(this::failLeft)

    fun <L: Throwable, R> failLeft(either: Either<L, R>): Future<R> =
        either
                .right()
                .map { Future.succeededFuture(it) }
                .right()
                .getOrElse {
                    either
                            .left()
                            .map { Future.failedFuture<R>(it) }
                            .left()
                            .get()
                }
}
