package playwrigkt.skript

import org.funktionale.either.Either
import org.funktionale.tries.Try
import playwrigkt.skript.ex.lift
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult

/**
 * Defines application logic in static functions.  Skripts expose an asynchronous interface for executing functions.
 * Skripts are not asynchronous  by default.  This class provides many pure skripts that in and of themselves provide
 * no asynchronous implementation.
 *
 * @param I is the input of this skript when it is run
 * @param O is the output of this skript when it is run
 * @param Troupe - interface for asynchronously accessing application resources at runtime.
 */
interface Skript<in I, O, Troupe> {

    /**
     * Run this skript.  This function can be called as many times as desired and will
     * never change the state of the skript.
     *
     * @param i the input
     * @param troupe allows access to application resources
     * @return an asynchronous computation
     */
    fun run(i: I, troupe: Troupe): AsyncResult<O>

    companion object {
        /**
         * Create a skript whose result is the input
         */
        fun <I, Troupe> identity(): Skript<I, I, Troupe> = Identity()

        /**
         * Create a skript that synchronously  runs a function that can access the  Troupe
         */
        fun <I, O, Troupe> mapWithTroupe(mapper: (I, Troupe) -> O): Skript<I, O, Troupe> = Map(mapper)

        /**
         * Create a skript that synchronously  runs a function
         */
        fun <I, O, Troupe> map(mapper: (I) -> O): Skript<I, O, Troupe> = mapWithTroupe({ o, _ -> mapper(o) })

        /**
         * create a skript that synchronously runs a function that can fail and can access the Troupe
         */
        fun <I, O, Troupe> mapTryWithTroupe(mapper: (I, Troupe) -> Try<O>): Skript<I, O, Troupe> = MapTry(mapper)

        /**
         * create a skript that synchronously runs a function that can fail
         */
        fun <I, O, Troupe> mapTry(mapper: (I) -> Try<O>): Skript<I, O, Troupe> = mapTryWithTroupe({ o, _ -> mapper(o) })

        /**
         * Create a skript that runs either `left` or `right` depending on `control`
         */
        fun <I, J, K, Troupe, O> branch(control: Skript<I, Either<J, K>, Troupe>, left: Skript<J, O, Troupe>, right: Skript<K, O, Troupe>): Skript<I, O, Troupe> = Branch(control, left, right)

        /**
         * Build a branching skript
         */
        fun <I, J, K, Troupe> branch(control: Skript<I, Either<J, K>, Troupe>): Branch.Builder<I, J, K, Troupe> = Branch.Builder.control(control)

        /**
         * Create a skript that runs two skripts in parallel
         */
        fun <I, L, R, Troupe> both(left: Skript<I, L, Troupe>, right: Skript<I, R, Troupe>): Skript<I, Pair<L, R>, Troupe> = Both(left, right)
    }

    /**
     * Chain to another skript
     */
    fun <O2> compose(skript: Skript<O, O2, Troupe>): Skript<I, O2, Troupe> = SkriptLink(this, skript)

    /**
     * Chain to an asynchronous function
     */
    fun <O2> flatMap(mapper: (O) -> AsyncResult<O2>): Skript<I, O2, Troupe> = this.flatMapWithTroupe { o, _ -> mapper(o) }

    /**
     * Chain to an asynchronous function that can access the Troupe
     */
    fun <O2> flatMapWithTroupe(mapper: (O, Troupe) -> AsyncResult<O2>): Skript<I, O2, Troupe> = this.compose(playwrigkt.skript.Skript.FlatMap(mapper))

    /**
     * Chain to a synchronous function that can access the Troupe
     */
    fun <O2> mapWithTroupe(mapper: (O, Troupe) -> O2): Skript<I, O2, Troupe> = this.compose(Map(mapper))

    /**
     * Chain to a synchronous function
     */
    fun <O2> map(mapper: (O) -> O2): Skript<I, O2, Troupe> = this.mapWithTroupe({ o, _ -> mapper(o) })

    /**
     * Chain to a synchronous function that can access the Troupe and can fail
     */
    fun <O2> mapTryWithTroupe(mapper: (O, Troupe) -> Try<O2>): Skript<I, O2, Troupe> = this.compose(MapTry(mapper))

    /**
     * Chain to a synchronous function that can fail
     */
    fun <O2> mapTry(mapper: (O) -> Try<O2>): Skript<I, O2, Troupe> = this.mapTryWithTroupe { o, _ -> mapper(o) }

    /**
     * Chain to a branching skript that performs either left or right depending on control
     */
    fun <J, K, O2> branch(control: Skript<O, Either<J, K>, Troupe>, left: Skript<J, O2, Troupe>, right: Skript<K, O2, Troupe>): Skript<I, O2, Troupe> =
        this.compose(Branch(control, left, right))

    /**
     * Chain to two skripts in parallel and collate the results
     */
    fun <L, R> both(left: Skript<O, L, Troupe>, right: Skript<O, R, Troupe>): Skript<I, Pair<L, R>, Troupe> = this.compose(Both(left, right))

    /**
     * Chain another skript, and collate its result with the result of this skript
     */
    fun <O2> split(other: Skript<O, O2, Troupe>): Skript<I, Pair<O, O2>, Troupe> = this.compose(Both(identity<O, Troupe>(), other))

    /**
     * Chain to `doOptionally` depending on whether the  result of `control` is `Right`
     */
    fun <J, O2>whenRight(doOptionally: Skript<J, O2, Troupe>, control: Skript<O, Either<O2, J>, Troupe>): Skript<I, O2, Troupe> =
        this.compose(Branch(control, identity(), doOptionally))

    /**
     * Chain to `doOptionally` dependingon whether the result of `control` is non-null
     */
    fun <J>whenNonNull(doOptionally: Skript<J, O, Troupe>, control: Skript<O, J?, Troupe>): Skript<I, O, Troupe> =
            this.compose(SkriptWhenNonNull(doOptionally, control))

    /**
     * Chain to `doOptionally` depending on whether the result of `control` is true
     */
    fun whenTrue(doOptionally: Skript<O, O, Troupe>, control: Skript<O, Boolean, Troupe>): Skript<I, O, Troupe> =
            this.compose(SkriptWhenTrue(doOptionally, control))

    data class SkriptIterate<I, O, Troupe>(val skript: Skript<I, O, Troupe>): Skript<List<I>, List<O>, Troupe> {
        override fun run(i: List<I>, troupe: Troupe): AsyncResult<List<O>> =
            i.map { skript.run(it, troupe) }
                    .lift()
    }
    /**
     * Wrap a troupe whose Troupe subclasses another troupe.  Allows a skript to be chained as long as its troupe
     * extends the current Troupe
     */
    data class Wrapped<I, O, Troupe: SubTroupe, SubTroupe>(val skript: Skript<I, O, SubTroupe>): Skript<I, O, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<O> {
            return skript.run(i, troupe)
        }
    }

    /**
     * Perform one skript and then another.
     *
     * A link that contains two skripts that have been chained together.  A chain is essentially a single linked list of skripts.
     */
    data class SkriptLink<I, J, O, Troupe>(val head: Skript<I, J, Troupe>, val tail: Skript<J, O, Troupe>): Skript<I, O, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<O> {
            return head.run(i, troupe).flatMap { tail.run(it, troupe) }
        }

        override fun <O2> compose(skript: Skript<O, O2, Troupe>): Skript<I, O2, Troupe> = SkriptLink(this.head, tail.compose(skript))
    }

    /**
     * No-op skipt, disappears when composed with other skripts.
     */
    class Identity<I, Troupe>: Skript<I, I, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<I> = AsyncResult.succeeded(i)
        override fun <O2> compose(skript: Skript<I, O2, Troupe>): Skript<I, O2, Troupe> = skript
    }

    /**
     * Perform a synchronous function
     */
    data class Map<I, O, Troupe>(val mapper: (I, Troupe) -> O): Skript<I, O, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<O> {
            val tri = Try { mapper(i, troupe) }
            return when(tri) {
                is Try.Failure -> CompletableResult.failed(tri.throwable)
                is Try.Success -> CompletableResult.succeeded(tri.get())
            }
        }
    }

    /**
     * Perform a synchronous function that may fail with an exception
     */
    data class MapTry<I, O, Troupe>(val mapper: (I, Troupe) -> Try<O>): Skript<I, O, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<O> {
            val tri = mapper(i, troupe)
            return when(tri) {
                is Try.Failure -> CompletableResult.failed(tri.throwable)
                is Try.Success -> CompletableResult.succeeded(tri.get())
            }
        }
    }

    /**
     * Perform an asynchronous function
     */
    data class FlatMap<I, O, Troupe>(val mapper: (I, Troupe) -> AsyncResult<O>): Skript<I, O, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<O> =
                mapper(i, troupe)
    }

    /**
     * perform two skripts and collate the results
     */
    data class Both<I, L, R, Troupe>(val left: Skript<I, L, Troupe>,
                                        val right: Skript<I, R, Troupe>): Skript<I, Pair<L, R>, Troupe> {
        override fun run(i: I, troupe: Troupe): AsyncResult<Pair<L, R>> {
            val leftResult = left.run(i, troupe)
            val rightResult = right.run(i, troupe)

            return leftResult.flatMap { l -> rightResult.map { r -> Pair(l, r) } }
        }
    }

    /**
     * Perform either `left` or `right` depending on `control`
     */
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

    /**
     * Perform `doOptionally` when `control` is non-null
     */
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

    /**
     * Perform `doOptionally` when `control` is true
     */
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
}