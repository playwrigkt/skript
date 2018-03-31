package playwrigkt.skript

import org.funktionale.either.Either
import org.funktionale.tries.Try
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult

interface Skript<in I, O, Stage> {
    fun run(i: I, stage: Stage): AsyncResult<O>

    companion object {
        fun <I, Stage> identity(): Skript<I, I, Stage> = map { it }
        fun <I, O, Stage> mapWithStage(mapper: (I, Stage) -> O): Skript<I, O, Stage> = Map(mapper)
        fun <I, O, Stage> map(mapper: (I) -> O): Skript<I, O, Stage> = mapWithStage({ o, _ -> mapper(o) })
        fun <I, O, Stage> mapTryWithStage(mapper: (I, Stage) -> Try<O>): Skript<I, O, Stage> = MapTry(mapper)
        fun <I, O, Stage> mapTry(mapper: (I) -> Try<O>): Skript<I, O, Stage> = mapTryWithStage({ o, _ -> mapper(o) })
        fun <I, J, K, Stage, O> branch(control: Skript<I, Either<J, K>, Stage>, left: Skript<J, O, Stage>, right: Skript<K, O, Stage>): Skript<I, O, Stage> = Branch(control, left, right)
        fun <I, J, K, Stage> branch(control: Skript<I, Either<J, K>, Stage>): Branch.Builder<I, J, K, Stage> = Branch.Builder.control(control)
        fun <I, Stage> updateStage(skript: Skript<I, Unit, Stage>): Skript<I, I, Stage> = UpdateStage(skript)
    }

    fun <O2> flatMap(skript: Skript<O, O2, Stage>): Skript<I, O2, Stage> = SkriptLink(this, skript)

    fun <O2> mapWithStage(mapper: (O, Stage) -> O2): Skript<I, O2, Stage> = this.flatMap(Map(mapper))
    fun <O2> map(mapper: (O) -> O2): Skript<I, O2, Stage> = this.mapWithStage({ o, _ -> mapper(o) })

    fun <O2> mapTryWithStage(mapper: (O, Stage) -> Try<O2>): Skript<I, O2, Stage> = this.flatMap(MapTry(mapper))
    fun <O2> mapTry(mapper: (O) -> Try<O2>): Skript<I, O2, Stage> = this.mapTryWithStage({ o, _ -> mapper(o) })

    fun updateStage(skript: Skript<O, Unit, Stage>): Skript<I, O, Stage> = this.flatMap(UpdateStage(skript))

    fun <J, K, O2> branch(control: Skript<O, Either<J, K>, Stage>, left: Skript<J, O2, Stage>, right: Skript<K, O2, Stage>): Skript<I, O2, Stage> =
        this.flatMap(Branch(control, left, right))
        
    fun <J, O2>whenRight(doOptionally: Skript<J, O2, Stage>, control: Skript<O, Either<O2, J>, Stage>): Skript<I, O2, Stage> =
        this.flatMap(Branch(control, identity(), doOptionally))

    fun <J>whenNonNull(doOptionally: Skript<J, O, Stage>, control: Skript<O, J?, Stage>): Skript<I, O, Stage> =
            this.flatMap(SkriptWhenNonNull(doOptionally, control))

    fun whenTrue(doOptionally: Skript<O, O, Stage>, control: Skript<O, Boolean, Stage>): Skript<I, O, Stage> =
            this.flatMap(SkriptWhenTrue(doOptionally, control))

    data class Wrapped<I, O, Stage: SubStage, SubStage>(val skript: Skript<I, O, SubStage>): Skript<I, O, Stage> {
        override fun run(i: I, stage: Stage): AsyncResult<O> {
            return skript.run(i, stage)
        }
    }
    /**
     * A link that contains two skripts that have been chained together.  A chain is essentially a single linked list of skripts.
     */
    data class SkriptLink<I, J, O, Stage>(val head: Skript<I, J, Stage>, val tail: Skript<J, O, Stage>): Skript<I, O, Stage> {
        override fun run(i: I, stage: Stage): AsyncResult<O> {
            return head.run(i, stage).flatMap { tail.run(it, stage) }
        }

        override fun <O2> flatMap(skript: Skript<O, O2, Stage>): Skript<I, O2, Stage> = SkriptLink(this.head, tail.flatMap(skript))
    }

    data class Map<I, O, Stage>(val mapper: (I, Stage) -> O): Skript<I, O, Stage> {
        override fun run(i: I, stage: Stage): AsyncResult<O> {
            val tri = Try { mapper(i, stage) }
            return when(tri) {
                is Try.Failure -> CompletableResult.failed(tri.throwable)
                is Try.Success -> CompletableResult.succeeded(tri.get())
            }
        }
    }

    data class MapTry<I, O, Stage>(val mapper: (I, Stage) -> Try<O>): Skript<I, O, Stage> {
        override fun run(i: I, stage: Stage): AsyncResult<O> {
            val tri = mapper(i, stage)
            return when(tri) {
                is Try.Failure -> CompletableResult.failed(tri.throwable)
                is Try.Success -> CompletableResult.succeeded(tri.get())
            }
        }
    }

    data class Branch<I, J, K, O, Stage>(val control: Skript<I, Either<J, K>, Stage>, val left: Skript<J, O, Stage>, val right: Skript<K, O, Stage>): Skript<I, O, Stage> {
        override fun run(i: I, stage: Stage): AsyncResult<O> {
            return control.run(i, stage)
                    .flatMap { when(it) {
                        is Either.Left -> left.run(it.l, stage)
                        is Either.Right -> right.run(it.r, stage)
                    } }
        }

        sealed class Builder<I, J, K, Stage> {
            abstract val control: Skript<I, Either<J, K>, Stage>

            companion object {
                fun <I, J, K, Stage> control(control: Skript<I, Either<J, K>, Stage>): Builder<I, J, K, Stage> = Impl(control)
            }

            fun <O> left(left: Skript<J, O, Stage>): Left<I, J, K, O, Stage> = Left(control, left)
            fun <O> right(right: Skript<K, O, Stage>): Right<I, J, K, O, Stage> = Right(control, right)

            private data class Impl<I, J, K, Stage>(override val control: Skript<I, Either<J, K>, Stage>): Builder<I, J, K, Stage>()

            data class Left<I, J, K, O, Stage>(val control: Skript<I, Either<J, K>, Stage>, val left: Skript<J, O, Stage>) {
                fun right(right: Skript<K, O, Stage>): Skript<I, O, Stage> = Branch(control, left, right)
            }
            
            data class Right<I, J, K, O, Stage>(val control: Skript<I, Either<J, K>, Stage>, val right: Skript<K, O, Stage>) {
                fun left(left: Skript<J, O, Stage>): Skript<I, O, Stage> = Branch(control, left, right)
            }
        }
    }

    data class SkriptWhenNonNull<I, J, Stage>(val doOptionally: Skript<J, I, Stage>, val control: Skript<I, J?, Stage>): Skript<I, I, Stage> {
        override fun run(i: I, stage: Stage): AsyncResult<I> {
            return control.run(i, stage)
                    .flatMap {
                        when(it) {
                            null -> CompletableResult.succeeded(i)
                            else -> doOptionally.run(it, stage)
                        }
                    }
        }
    }

    data class SkriptWhenTrue<I, Stage>(val doOptionally: Skript<I, I, Stage>, val control: Skript<I, Boolean, Stage>): Skript<I, I, Stage> {
        override fun run(i: I, stage: Stage): AsyncResult<I> {
            return control.run(i, stage)
                    .flatMap {
                        when(it) {
                            true -> doOptionally.run(i, stage)
                            else -> CompletableResult.succeeded(i)
                        }
                    }
        }
    }

    data class UpdateStage<I, Stage>(val skript: Skript<I, Unit, Stage>): Skript<I, I, Stage> {
        override fun run(i: I, stage: Stage): AsyncResult<I> {
            return skript.run(i, stage)
                    .map { i }
        }

    }
}