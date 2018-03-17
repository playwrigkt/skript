package dev.yn.playground

import dev.yn.playground.result.AsyncResult
import dev.yn.playground.result.CompletableResult
import org.funktionale.either.Either
import org.funktionale.tries.Try

interface Skript<in I, O, C> {
    fun run(i: I, context: C): AsyncResult<O>

    companion object {
        fun <I, C> identity(): Skript<I, I, C> = map { it }
        fun <I, O, C> mapWithContext(mapper: (I, C) -> O): Skript<I, O, C> = Map(mapper)
        fun <I, O, C> map(mapper: (I) -> O): Skript<I, O, C> = mapWithContext({ o, context -> mapper(o) })
        fun <I, O, C> mapTryWithContext(mapper: (I, C) -> Try<O>): Skript<I, O, C> = MapTry(mapper)
        fun <I, O, C> mapTry(mapper: (I) -> Try<O>): Skript<I, O, C> = mapTryWithContext({ o, context -> mapper(o) })
        fun <I, J, K, C, O> branch(control: Skript<I, Either<J, K>, C>, left: Skript<J, O, C>, right: Skript<K, O, C>): Skript<I, O, C> = Branch(control, left, right)
        fun <I, J, K, C> branch(control: Skript<I, Either<J, K>, C>): Branch.Builder<I, J, K, C> = Branch.Builder.control(control)
        fun <I, C> updateContext(skript: Skript<I, Unit, C>): Skript<I, I, C> = UpdateContext(skript)
    }

//    fun <O2> andThen(skript: Skript<O, O2, C>): Skript<I, O2, C> = SkriptLink(this, skript)
    fun <O2> flatMap(skript: Skript<O, O2, C>): Skript<I, O2, C> = SkriptLink(this, skript)

    fun <O2> mapWithContext(mapper: (O, C) -> O2): Skript<I, O2, C> = this.flatMap(Map(mapper))
    fun <O2> map(mapper: (O) -> O2): Skript<I, O2, C> = this.mapWithContext({ o, context -> mapper(o) })

    fun <O2> mapTryWithContext(mapper: (O, C) -> Try<O2>): Skript<I, O2, C> = this.flatMap(MapTry(mapper))
    fun <O2> mapTry(mapper: (O) -> Try<O2>): Skript<I, O2, C> = this.mapTryWithContext({ o, context -> mapper(o) })

    fun updateContext(skript: Skript<O, Unit, C>): Skript<I, O, C> = this.flatMap(UpdateContext(skript))

    fun <J, K, O2> branch(control: Skript<O, Either<J, K>, C>, left: Skript<J, O2, C>, right: Skript<K, O2, C>): Skript<I, O2, C> =
        this.flatMap(Branch(control, left, right))
        
    fun <J, O2>whenRight(doOptionally: Skript<J, O2, C>, control: Skript<O, Either<O2, J>, C>): Skript<I, O2, C> =
        this.flatMap(Branch(control, identity(), doOptionally))

    fun <J>whenNonNull(doOptionally: Skript<J, O, C>, control: Skript<O, J?, C>): Skript<I, O, C> =
            this.flatMap(SkriptWhenNonNull(doOptionally, control))

    fun whenTrue(doOptionally: Skript<O, O, C>, control: Skript<O, Boolean, C>): Skript<I, O, C> =
            this.flatMap(SkriptWhenTrue(doOptionally, control))

    data class Wrapped<I, O, C: CP, CP>(val skript: Skript<I, O, CP>): Skript<I, O, C> {
        override fun run(i: I, context: C): AsyncResult<O> {
            return skript.run(i, context)
        }
    }
    /**
     * A link that contains two tasks that have been chained together.  A chain is essentially a single linked list of tasks.
     */
    data class SkriptLink<I, J, O, C>(val head: Skript<I, J, C>, val tail: Skript<J, O, C>): Skript<I, O, C> {
        override fun run(i: I, context: C): AsyncResult<O> {
            return head.run(i, context).flatMap { tail.run(it, context) }
        }

        override fun <O2> flatMap(skript: Skript<O, O2, C>): Skript<I, O2, C> = SkriptLink(this.head, tail.flatMap(skript))
    }

    data class Map<I, O, C>(val mapper: (I, C) -> O): Skript<I, O, C> {
        override fun run(i: I, context: C): AsyncResult<O> {
            val tri = Try { mapper(i, context) }
            return when(tri) {
                is Try.Failure -> CompletableResult.failed(tri.throwable)
                is Try.Success -> CompletableResult.succeeded(tri.get())
            }
        }
    }

    data class MapTry<I, O, C>(val mapper: (I, C) -> Try<O>): Skript<I, O, C> {
        override fun run(i: I, context: C): AsyncResult<O> {
            val tri = mapper(i, context)
            return when(tri) {
                is Try.Failure -> CompletableResult.failed(tri.throwable)
                is Try.Success -> CompletableResult.succeeded(tri.get())
            }
        }
    }

    data class Branch<I, J, K, O, C>(val control: Skript<I, Either<J, K>, C>, val left: Skript<J, O, C>, val right: Skript<K, O, C>): Skript<I, O, C> {
        override fun run(i: I, context: C): AsyncResult<O> {
            return control.run(i, context)
                    .flatMap { when(it) {
                        is Either.Left -> left.run(it.l, context)
                        is Either.Right -> right.run(it.r, context)
                    } }
        }

        sealed class Builder<I, J, K, C> {
            abstract val control: Skript<I, Either<J, K>, C>

            companion object {
                fun <I, J, K, C> control(control: Skript<I, Either<J, K>, C>): Builder<I, J, K, C> = Impl(control)
            }

            fun <O> left(left: Skript<J, O, C>): Left<I, J, K, O, C> = Left(control, left)
            fun <O> right(right: Skript<K, O, C>): Right<I, J, K, O, C> = Right(control, right)
            fun <O> ifLeft(left: Skript<J, O, C>): Left<I, J, K, O, C> = left(left)
            fun <O> ifRight(right: Skript<K, O, C>): Right<I, J, K, O, C> = right(right)

            private data class Impl<I, J, K, C>(override val control: Skript<I, Either<J, K>, C>): Builder<I, J, K, C>()

            data class Left<I, J, K, O, C>(val control: Skript<I, Either<J, K>, C>, val left: Skript<J, O, C>) {
                fun right(right: Skript<K, O, C>): Skript<I, O, C> = Branch(control, left, right)
                fun ifRight(right: Skript<K, O, C>): Skript<I, O, C> = right(right)
            }
            
            data class Right<I, J, K, O, C>(val control: Skript<I, Either<J, K>, C>, val right: Skript<K, O, C>) {
                fun left(left: Skript<J, O, C>): Skript<I, O, C> = Branch(control, left, right)
                fun ifLeft(left: Skript<J, O, C>): Skript<I, O, C> = left(left)
            }
        }
    }

    data class SkriptWhenNonNull<I, J, C>(val doOptionally: Skript<J, I, C>, val control: Skript<I, J?, C>): Skript<I, I, C> {
        override fun run(i: I, context: C): AsyncResult<I> {
            return control.run(i, context)
                    .flatMap {
                        when(it) {
                            null -> CompletableResult.succeeded(i)
                            else -> doOptionally.run(it, context)
                        }
                    }
        }
    }

    data class SkriptWhenTrue<I, C>(val doOptionally: Skript<I, I, C>, val control: Skript<I, Boolean, C>): Skript<I, I, C> {
        override fun run(i: I, context: C): AsyncResult<I> {
            return control.run(i, context)
                    .flatMap {
                        when(it) {
                            true -> doOptionally.run(i, context)
                            else -> CompletableResult.succeeded(i)
                        }
                    }
        }
    }

    data class UpdateContext<I, C>(val skript: Skript<I, Unit, C>): Skript<I, I, C> {
        override fun run(i: I, context: C): AsyncResult<I> {
            return skript.run(i, context)
                    .map { i }
        }

    }
}