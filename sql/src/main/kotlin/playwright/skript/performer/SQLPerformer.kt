package playwright.skript.performer

import playwright.skript.result.AsyncResult
import playwright.skript.sql.SQLCommand
import playwright.skript.sql.SQLResult

abstract class SQLPerformer {
    abstract fun <T> close(): (T) -> AsyncResult<T>
    abstract fun <T> closeOnFailure(): (Throwable) -> AsyncResult<T>
    abstract fun <T> commit(): (T) -> AsyncResult<T>
    abstract fun <T> rollback(): (Throwable) -> AsyncResult<T>
    abstract fun setAutoCommit(autoCommit: Boolean): AsyncResult<Unit>

    abstract fun query(query: SQLCommand.Query): AsyncResult<SQLResult.Query>
    abstract fun update(update: SQLCommand.Update): AsyncResult<SQLResult.Update>
    abstract fun exec(exec: SQLCommand.Exec): AsyncResult<SQLResult.Void>
}