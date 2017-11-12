package dev.yn.playground.sql.context

import dev.yn.playground.task.result.AsyncResult

interface SQLTaskContextProvider<C: SQLExecutor> {
    fun getConnection(): AsyncResult<C>
}

interface SQLTaskContext<C: SQLExecutor> {
    fun getSQLActionContext(): C
}