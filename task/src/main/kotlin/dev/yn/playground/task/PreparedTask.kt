package dev.yn.playground.task

import io.vertx.core.Future

data class PreparedTask<I, O, T>(val action: (I, T) -> Future<O>, val provided: T): Task<I, O> {
    override fun run(i: I): Future<O> {
        return action(i, provided)
    }
}