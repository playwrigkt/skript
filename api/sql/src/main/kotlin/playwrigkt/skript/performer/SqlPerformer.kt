package playwrigkt.skript.performer

import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.sql.SqlCommand
import playwrigkt.skript.sql.SqlResult

abstract class SqlPerformer {
    abstract fun <T> close(): (T) -> AsyncResult<T>
    abstract fun <T> closeOnFailure(): (Throwable) -> AsyncResult<T>
    abstract fun <T> commit(): (T) -> AsyncResult<T>
    abstract fun <T> rollback(): (Throwable) -> AsyncResult<T>
    abstract fun setAutoCommit(autoCommit: Boolean): AsyncResult<Unit>

    abstract fun query(query: SqlCommand.Query): AsyncResult<SqlResult.Query>
    abstract fun update(update: SqlCommand.Update): AsyncResult<SqlResult.Update>
    abstract fun exec(exec: SqlCommand.Exec): AsyncResult<SqlResult.Void>
}