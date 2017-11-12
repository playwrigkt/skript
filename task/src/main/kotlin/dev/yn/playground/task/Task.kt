package dev.yn.playground.task

import dev.yn.playground.task.result.AsyncResult
import dev.yn.playground.task.result.CompletableResult
import org.funktionale.either.Either
import org.funktionale.tries.Try

interface Task<I, O, C> {

    fun run(i: I, context: C): AsyncResult<O>

    companion object {
        fun <I, O, C> mapWithContext(mapper: (I, C) -> O): Task<I, O, C> = Map(mapper)
        fun <I, O, C> map(mapper: (I) -> O): Task<I, O, C> = mapWithContext({o, context -> mapper(o) })
    }

    fun <O2> andThen(task: Task<O, O2, C>): Task<I, O2, C> = TaskLink(this, task)
    fun <O2> flatMap(task: Task<O, O2, C>): Task<I, O2, C> = this.andThen(task)

    fun <O2> mapWithContext(mapper: (O, C) -> O2): Task<I, O2, C> = this.andThen(Map(mapper))
    fun <O2> map(mapper: (O) -> O2): Task<I, O2, C> = this.mapWithContext({ o, context -> mapper(o) })

    fun <O2> mapTryWithContext(mapper: (O, C) -> Try<O2>): Task<I, O2, C> = this.andThen(MapTry(mapper))
    fun <O2> mapTry(mapper: (O) -> Try<O2>): Task<I, O2, C> = this.mapTryWithContext({ o, context -> mapper(o) })

    fun <J, O2>whenRight(doOptionally: Task<J, O2, C>, whenRight: Task<O, Either<O2, J>, C>): Task<I, O2, C> =
            this.andThen(TaskWhenRight(doOptionally, whenRight))

    fun <J>whenNonNull(doOptionally: Task<J, O, C>, whenNonNull: Task<O, J?, C>): Task<I, O, C> =
            this.andThen(TaskWhenNonNull(doOptionally, whenNonNull))

    fun whenTrue(doOptionally: Task<O, O, C>, whenTrue: Task<O, Boolean, C>): Task<I, O, C> =
            this.andThen(TaskWhenTrue(doOptionally, whenTrue))

    /**
     * A link that contains two tasks that have been chained together.  A chain is essentially a single linked list of tasks.
     */
    data class TaskLink<I, J, O, C>(val head: Task<I, J, C>, val tail: Task<J, O, C>): Task<I, O, C> {
        override fun run(i: I, context: C): AsyncResult<O> {
            return head.run(i, context).flatMap { tail.run(it, context) }
        }

        override fun <O2> andThen(task: Task<O, O2, C>): Task<I, O2, C> = TaskLink(this.head, tail.andThen(task))
    }

    private data class Map<I, O, C>(val mapper: (I, C) -> O): Task<I, O, C> {
        override fun run(i: I, context: C): AsyncResult<O> {
            val tri = Try { mapper(i, context) }
            return when(tri) {
                is Try.Failure -> CompletableResult.failed(tri.throwable)
                is Try.Success -> CompletableResult.succeeded(tri.get())
            }
        }
    }

    private data class MapTry<I, O, C>(val mapper: (I, C) -> Try<O>): Task<I, O, C> {
        override fun run(i: I, context: C): AsyncResult<O> {
            val tri = mapper(i, context)
            return when(tri) {
                is Try.Failure -> CompletableResult.failed(tri.throwable)
                is Try.Success -> CompletableResult.succeeded(tri.get())
            }
        }
    }

    private data class TaskWhenRight<I, J, O, C>(val doOptionally: Task<J, O, C>, val whenRight: Task<I, Either<O, J>, C>): Task<I, O, C> {
        override fun run(i: I, context: C): AsyncResult<O> {
            return whenRight.run(i, context)
                    .flatMap {
                        when(it) {
                            is Either.Left -> it.left().get().let { CompletableResult.succeeded(it) }
                            is Either.Right -> it.right().get().let { doOptionally.run(it, context) }
                        }
                    }
        }
    }

    data class TaskWhenNonNull<I, J, C>(val doOptionally: Task<J, I, C>, val whenNonNull: Task<I, J?, C>): Task<I, I, C> {
        override fun run(i: I, context: C): AsyncResult<I> {
            return whenNonNull.run(i, context)
                    .flatMap {
                        when(it) {
                            null -> CompletableResult.succeeded(i)
                            else -> doOptionally.run(it, context)
                        }
                    }
        }
    }

    data class TaskWhenTrue<I, C>(val doOptionally: Task<I, I, C>, val whenTrue: Task<I, Boolean, C>): Task<I, I, C> {
        override fun run(i: I, context: C): AsyncResult<I> {
            return whenTrue.run(i, context)
                    .flatMap {
                        when(it) {
                            true -> doOptionally.run(i, context)
                            else -> CompletableResult.succeeded(i)
                        }
                    }
        }
    }
}