package dev.yn.playground.context

import dev.yn.playground.result.AsyncResult
import dev.yn.playground.sql.SQLExecutor

interface SQLSkriptContextProvider<C: SQLExecutor> {
    fun getConnection(): AsyncResult<C>
}

interface SQLSkriptContext<C: SQLExecutor> {
    fun getSQLExecutor(): C
}