package dev.yn.playground.task.session

import dev.yn.playground.task.*
import io.vertx.core.Future
import org.funktionale.either.Either
import org.funktionale.tries.Try

interface SessionOperation<I, O, SessionContext> {
    fun session(context: SessionContext): Task<I, O>

    fun <O2> andThen(sessionOperaton: SessionOperation<O, O2, SessionContext>): SessionOperation<I, O2, SessionContext> {
        return SessionOperationLink(this, sessionOperaton)
    }

}

/**
 * A link that contains two unprepared tasks that have been chained together.  A chain is essentially a single linked list of unprepared tasks.
 */
data class SessionOperationLink<I, J, O, SessionContext>(val head: SessionOperation<I, J, SessionContext>, val tail: SessionOperation<J, O, SessionContext>): SessionOperation<I, O, SessionContext> {
    override fun session(context: SessionContext): Task<I, O> {
        return head.session(context).andThen(tail.session(context))
    }

    override fun <O2> andThen(sessionOperation: SessionOperation<O, O2, SessionContext>): SessionOperation<I, O2, SessionContext> {
        return SessionOperationLink(head, tail.andThen(sessionOperation))
    }
}

data class SessionOperationWhenRight<I, J, O, SessionContext>(val doOptionally: SessionOperation<J, O, SessionContext>, val whenRight: SessionOperation<I, Either<O, J>, SessionContext>): SessionOperation<I, O, SessionContext> {
    override fun session(context: SessionContext): Task<I, O> {
        return TaskWhenRight(doOptionally.session(context), whenRight.session(context))
    }
}

data class SessionOperationTaskWhenNonNull<I, J, SessionContext>(val doOptionally: SessionOperation<J, I, SessionContext>, val whenNonNull: SessionOperation<I, J?, SessionContext>): SessionOperation<I, I, SessionContext> {
    override fun session(context: SessionContext): Task<I, I> {
        return TaskWhenNonNull(doOptionally.session(context), whenNonNull.session(context))
    }
}

data class SessionOperationWhenTrue<I, SessionContext>(val doOptionally: SessionOperation<I, I, SessionContext>, val whenTrue: SessionOperation<I, Boolean, SessionContext>): SessionOperation<I, I, SessionContext> {
    override fun session(context: SessionContext): Task<I, I> {
        return TaskWhenTrue(doOptionally.session(context), whenTrue.session(context))
    }
}
data class SessionOperationSync<I, O, SessionContext>(val mapper: (I) -> Try<O>): SessionOperation<I, O, SessionContext> {
    override fun session(context: SessionContext): Task<I, O> {
        return SyncTask(mapper)
    }
}
data class DefaultSessionOperation<I, O, SessionContext>(val action: (I, SessionContext) -> Future<O>): SessionOperation<I, O, SessionContext> {
    override fun session(context: SessionContext): Task<I, O> {
        return SessionTask(action, context)
    }
}

class SessionTask<I, O, SessionContext>(val action: (I, SessionContext) -> Future<O>, val context: SessionContext): Task<I, O> {
    override fun run(i: I): Future<O> = action(i, context)
}
