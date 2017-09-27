package dev.yn.playground.task

import org.funktionale.either.Either
import org.funktionale.tries.Try

/**
 * A doOptionally that needs to have something injected into it with a PROVIDER, for example a vertx instance or a jdbcConnection
 */
interface UnpreparedTask<I, O, PROVIDER> {
    fun prepare(p: PROVIDER): Task<I, O>

    fun <O2> andThen(unpreparedTask: UnpreparedTask<O, O2, PROVIDER>): UnpreparedTask<I, O2, PROVIDER> {
        return UnpreparedTaskLink(this, unpreparedTask)
    }
}

/**
 * A link that contains two unprepared tasks that have been chained together.  A chain is essentially a single linked list of unprepared tasks.
 */
data class UnpreparedTaskLink<I, J, O, PROVIDER>(val head: UnpreparedTask<I, J, PROVIDER>, val tail: UnpreparedTask<J, O, PROVIDER>): UnpreparedTask<I, O, PROVIDER> {
    override fun prepare(p: PROVIDER): Task<I, O> {
        return head.prepare(p).andThen(tail.prepare(p))
    }

    override fun <O2> andThen(unpreparedTask: UnpreparedTask<O, O2, PROVIDER>): UnpreparedTask<I, O2, PROVIDER> {
        return UnpreparedTaskLink(head, tail.andThen(unpreparedTask))
    }
}

data class UnpreparedOptionalTask<I, J, O, PROVIDER>(val doOptionally: UnpreparedTask<J, O, PROVIDER>, val whenRight: UnpreparedTask<I, Either<O, J>, PROVIDER>): UnpreparedTask<I, O, PROVIDER> {
    override fun prepare(p: PROVIDER): Task<I, O> {
        return OptionalTask(doOptionally.prepare(p), whenRight.prepare(p))
    }
}

data class UnpreparedSyncTask<I, O, PROVIDER>(val mapper: (I) -> Try<O>): UnpreparedTask<I, O, PROVIDER> {
    override fun prepare(p: PROVIDER): Task<I, O> {
        return SyncTask(mapper)
    }
}