package playwrigkt.skript

import org.funktionale.either.Either
import org.funktionale.tries.Try
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult

interface Skript<in I, O, Troupe> {
    fun run(i: I, troupe: Troupe): AsyncResult<O>

    companion object {
        fun <I, Troupe> identity(): Skript<I, I, Troupe> = map { it }
        fun <I, O, Troupe> mapWithTroupe(mapper: (I, Troupe) -> O): Skript<I, O, Troupe> = Map(mapper)
        fun <I, O, Troupe> map(mapper: (I) -> O): Skript<I, O, Troupe> = mapWithTroupe({ o, _ -> mapper(o) })
        fun <I, O, Troupe> mapTryWithTroupe(mapper: (I, Troupe) -> Try<O>): Skript<I, O, Troupe> = MapTry(mapper)
        fun <I, O, Troupe> mapTry(mapper: (I) -> Try<O>): Skript<I, O, Troupe> = mapTryWithTroupe({ o, _ -> mapper(o) })
        fun <I, J, K, Troupe, O> branch(control: Skript<I, Either<J, K>, Troupe>, left: Skript<J, O, Troupe>, right: Skript<K, O, Troupe>): Skript<I, O, Troupe> = Branch(control, left, right)
        fun <I, J, K, Troupe> branch(control: Skript<I, Either<J, K>, Troupe>): Branch.Builder<I, J, K, Troupe> = Branch.Builder.control(control)
        fun <I, Troupe> updateTroupe(skript: Skript<I, Unit, Troupe>): Skript<I, I, Troupe> = UpdateTroupe(skript)
    }

    fun <O2> flatMap(skript: Skript<O, O2, Troupe>): Skript<I, O2, Troupe> = SkriptLink(this, skript)

    fun <O2> mapWithTroupe(mapper: (O, Troupe) -> O2): Skript<I, O2, Troupe> = this.flatMap(Map(mapper))
    fun <O2> map(mapper: (O) -> O2): Skript<I, O2, Troupe> = this.mapWithTroupe({ o, _ -> mapper(o) })

    fun <O2> mapTryWithTroupe(mapper: (O, Troupe) -> Try<O2>): Skript<I, O2, Troupe> = this.flatMap(MapTry(mapper))
    fun <O2> mapTry(mapper: (O) -> Try<O2>): Skript<I, O2, Troupe> = this.mapTryWithTroupe({ o, _ -> mapper(o) })

    fun updateTroupe(skript: Skript<O, Unit, Troupe>): Skript<I, O, Troupe> = this.flatMap(UpdateTroupe(skript))

    fun <J, K, O2> branch(control: Skript<O, Either<J, K>, Troupe>, left: Skript<J, O2, Troupe>, right: Skript<K, O2, Troupe>): Skript<I, O2, Troupe> =
        this.flatMap(Branch(control, left, right))
        
    fun <J, O2>whenRight(doOptionally: Skript<J, O2, Troupe>, control: Skript<O, Either<O2, J>, Troupe>): Skript<I, O2, Troupe> =
        this.flatMap(Branch(control, identity(), doOptionally))

    fun <J>whenNonNull(doOptionally: Skript<J, O, Troupe>, control: Skript<O, J?, Troupe>): Skript<I, O, Troupe> =
            this.flatMap(SkriptWhenNonNull(doOptionally, control))

    fun whenTrue(doOptionally: Skript<O, O, Troupe>, control: Skript<O, Boolean, Troupe>): Skript<I, O, Troupe> =
            this.flatMap(SkriptWhenTrue(doOptionally, control))

    data class Wrapped<I, O, Troupe: SubTroupe, SubTroupe>(val skript: Skript<I, O, SubTroupe>): Skript<I, O, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<O> {
            return skript.run(i, troupe)
        }
    }
    /**
     * A link that contains two skripts that have been chained together.  A chain is essentially a single linked list of skripts.
     */
    data class SkriptLink<I, J, O, Troupe>(val head: Skript<I, J, Troupe>, val tail: Skript<J, O, Troupe>): Skript<I, O, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<O> {
            return head.run(i, troupe).flatMap { tail.run(it, troupe) }
        }

        override fun <O2> flatMap(skript: Skript<O, O2, Troupe>): Skript<I, O2, Troupe> = SkriptLink(this.head, tail.flatMap(skript))
    }

    data class Map<I, O, Troupe>(val mapper: (I, Troupe) -> O): Skript<I, O, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<O> {
            val tri = Try { mapper(i, troupe) }
            return when(tri) {
                is Try.Failure -> CompletableResult.failed(tri.throwable)
                is Try.Success -> CompletableResult.succeeded(tri.get())
            }
        }
    }

    data class MapTry<I, O, Troupe>(val mapper: (I, Troupe) -> Try<O>): Skript<I, O, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<O> {
            val tri = mapper(i, troupe)
            return when(tri) {
                is Try.Failure -> CompletableResult.failed(tri.throwable)
                is Try.Success -> CompletableResult.succeeded(tri.get())
            }
        }
    }

    data class Branch<I, J, K, O, Troupe>(val control: Skript<I, Either<J, K>, Troupe>, val left: Skript<J, O, Troupe>, val right: Skript<K, O, Troupe>): Skript<I, O, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<O> {
            return control.run(i, troupe)
                    .flatMap { when(it) {
                        is Either.Left -> left.run(it.l, troupe)
                        is Either.Right -> right.run(it.r, troupe)
                    } }
        }

        sealed class Builder<I, J, K, Troupe> {
            abstract val control: Skript<I, Either<J, K>, Troupe>

            companion object {
                fun <I, J, K, Troupe> control(control: Skript<I, Either<J, K>, Troupe>): Builder<I, J, K, Troupe> = Impl(control)
            }

            fun <O> left(left: Skript<J, O, Troupe>): Left<I, J, K, O, Troupe> = Left(control, left)
            fun <O> right(right: Skript<K, O, Troupe>): Right<I, J, K, O, Troupe> = Right(control, right)

            private data class Impl<I, J, K, Troupe>(override val control: Skript<I, Either<J, K>, Troupe>): Builder<I, J, K, Troupe>()

            data class Left<I, J, K, O, Troupe>(val control: Skript<I, Either<J, K>, Troupe>, val left: Skript<J, O, Troupe>) {
                fun right(right: Skript<K, O, Troupe>): Skript<I, O, Troupe> = Branch(control, left, right)
            }
            
            data class Right<I, J, K, O, Troupe>(val control: Skript<I, Either<J, K>, Troupe>, val right: Skript<K, O, Troupe>) {
                fun left(left: Skript<J, O, Troupe>): Skript<I, O, Troupe> = Branch(control, left, right)
            }
        }
    }

    data class SkriptWhenNonNull<I, J, Troupe>(val doOptionally: Skript<J, I, Troupe>, val control: Skript<I, J?, Troupe>): Skript<I, I, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<I> {
            return control.run(i, troupe)
                    .flatMap {
                        when(it) {
                            null -> CompletableResult.succeeded(i)
                            else -> doOptionally.run(it, troupe)
                        }
                    }
        }
    }

    data class SkriptWhenTrue<I, Troupe>(val doOptionally: Skript<I, I, Troupe>, val control: Skript<I, Boolean, Troupe>): Skript<I, I, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<I> {
            return control.run(i, troupe)
                    .flatMap {
                        when(it) {
                            true -> doOptionally.run(i, troupe)
                            else -> CompletableResult.succeeded(i)
                        }
                    }
        }
    }

    data class UpdateTroupe<I, Troupe>(val skript: Skript<I, Unit, Troupe>): Skript<I, I, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<I> {
            return skript.run(i, troupe)
                    .map { i }
        }

    }
}