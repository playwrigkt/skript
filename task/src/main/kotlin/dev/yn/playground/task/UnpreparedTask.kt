package dev.yn.playground.task

import io.vertx.core.Future
import io.vertx.core.Vertx
import org.funktionale.either.Either
import org.funktionale.tries.Try

/**
 * A doOptionally that needs to have something injected into it with a PROVIDER, for example a vertx instance or a jdbcConnection
 */
interface UnpreparedTask<I, O, PROVIDER> {
    companion object {
        fun <I, O, PROVIDER: VertxProvider> vertxTask(vertxAction: (I, Vertx) -> Future<O>): UnpreparedTask<I, O, PROVIDER> =
                UnpreparedVertxTask(vertxAction)

        fun <I, J, O, PROVIDER> whenRight(doOptionally: UnpreparedTask<J, O, PROVIDER>, whenRight: UnpreparedTask<I, Either<O, J>, PROVIDER>): UnpreparedTask<I, O, PROVIDER> =
                UnpreparedTaskWhenRight(doOptionally, whenRight)

        fun <I, J, PROVIDER> whenNonNull(doOptionally: UnpreparedTask<J, I, PROVIDER>, whenNonNull: UnpreparedTask<I, J?, PROVIDER>): UnpreparedTask<I, I, PROVIDER> =
                UnpreparedTaskWhenNonNull(doOptionally, whenNonNull)

        fun <I, PROVIDER> whenTrue(doOptionally: UnpreparedTask<I, I, PROVIDER>, whenTrue: UnpreparedTask<I, Boolean, PROVIDER>): UnpreparedTask<I, I, PROVIDER> =
                UnpreparedTaskWhenTrue(doOptionally, whenTrue)

        fun <I, O, PROVIDER> map(mapper: (I) -> Try<O>): UnpreparedTask<I, O, PROVIDER> =
                UnpreparedSyncTask(mapper)
    }
    fun prepare(p: PROVIDER): Task<I, O>

    fun <O2> andThen(unpreparedTask: UnpreparedTask<O, O2, PROVIDER>): UnpreparedTask<I, O2, PROVIDER> {
        return UnpreparedTaskLink(this, unpreparedTask)
    }

    fun <J, K> whenRight(doOptionally: UnpreparedTask<J, K, PROVIDER>, whenRight: UnpreparedTask<O, Either<K, J>, PROVIDER>): UnpreparedTask<I, K, PROVIDER> =
            andThen(UnpreparedTaskWhenRight(doOptionally, whenRight))

    fun <J> whenNonNull(doOptionally: UnpreparedTask<J, O, PROVIDER>, whenNonNull: UnpreparedTask<O, J?, PROVIDER>): UnpreparedTask<I, O, PROVIDER> =
            andThen(UnpreparedTaskWhenNonNull(doOptionally, whenNonNull))

    fun whenTrue(doOptionally: UnpreparedTask<O, O, PROVIDER>, whenTrue: UnpreparedTask<O, Boolean, PROVIDER>): UnpreparedTask<I, O, PROVIDER> =
            andThen(UnpreparedTaskWhenTrue(doOptionally, whenTrue))

    fun <K> map(mapper: (O) -> Try<K>): UnpreparedTask<I, K, PROVIDER> =
            andThen(UnpreparedSyncTask(mapper))
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

data class UnpreparedTaskWhenRight<I, J, O, PROVIDER>(val doOptionally: UnpreparedTask<J, O, PROVIDER>, val whenRight: UnpreparedTask<I, Either<O, J>, PROVIDER>): UnpreparedTask<I, O, PROVIDER> {
    override fun prepare(p: PROVIDER): Task<I, O> {
        return TaskWhenRight(doOptionally.prepare(p), whenRight.prepare(p))
    }
}

data class UnpreparedTaskWhenNonNull<I, J, PROVIDER>(val doOptionally: UnpreparedTask<J, I, PROVIDER>, val whenNonNull: UnpreparedTask<I, J?, PROVIDER>): UnpreparedTask<I, I, PROVIDER> {
    override fun prepare(p: PROVIDER): Task<I, I> {
        return TaskWhenNonNull(doOptionally.prepare(p), whenNonNull.prepare(p))
    }
}

data class UnpreparedTaskWhenTrue<I, PROVIDER>(val doOptionally: UnpreparedTask<I, I, PROVIDER>, val whenTrue: UnpreparedTask<I, Boolean, PROVIDER>): UnpreparedTask<I, I, PROVIDER> {
    override fun prepare(p: PROVIDER): Task<I, I> {
        return TaskWhenTrue(doOptionally.prepare(p), whenTrue.prepare(p))
    }
}
data class UnpreparedSyncTask<I, O, PROVIDER>(val mapper: (I) -> Try<O>): UnpreparedTask<I, O, PROVIDER> {
    override fun prepare(p: PROVIDER): Task<I, O> {
        return SyncTask(mapper)
    }
}