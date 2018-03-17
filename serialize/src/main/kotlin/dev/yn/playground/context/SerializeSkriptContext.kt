package dev.yn.playground.context

import dev.yn.playground.result.AsyncResult
import dev.yn.playground.serialize.SerializeSkriptExecutor

interface SerializeSkriptContextProvider<E: SerializeSkriptExecutor> {
    fun getSerializeSkriptExecutor(): AsyncResult<E>
}

interface SerializeSkriptContext<E: SerializeSkriptExecutor> {
    fun getSerializeSkriptExecutor(): E
}