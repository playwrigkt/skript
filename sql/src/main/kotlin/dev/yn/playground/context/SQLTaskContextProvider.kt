package dev.yn.playground.context

import dev.yn.playground.result.AsyncResult
import dev.yn.playground.sql.SQLExecutor

interface SQLTaskContextProvider<C: SQLExecutor> {
    fun getConnection(): AsyncResult<C>
}

interface SQLTaskContext<C: SQLExecutor> {
    fun getSQLExecutor(): C
}