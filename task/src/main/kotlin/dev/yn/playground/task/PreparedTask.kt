package dev.yn.playground.task

import io.vertx.core.Future

/**
 * An unprepared task that has been prepared (i.e. dependencies injected
 */
class PreparedTask<I, O, T>(val action: (I, T) -> Future<O>, val provided: T): Task<I, O> {
    override fun run(i: I): Future<O> {
        return action(i, provided)
    }
}

data class PreparedTaskLink<I, J, O>(val preparedTask: Task<I, J>, val next: Task<J, O>): Task<I, O> {
    override fun run(i: I): Future<O> {
        return preparedTask.run(i)
                .compose(next::run)
    }
}