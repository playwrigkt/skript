package playwrigkt.pkrase

import playwrigkt.skript.troupe.QueuePublishTroupe
import playwrigkt.skript.Skript
import playwrigkt.skript.troupe.SQLTroupe
import playwrigkt.skript.troupe.SerializeTroupe

fun main(args: Array<String>) {
    println(one.evaluate())
    println((one plus one).evaluate())
    println((one plus one plus one).evaluate())
    val math = (one plus one) plus (half of one)
    println(math.evaluate())
    println((half of one plus one plus one).evaluate())

    println((half of (one plus one plus one)).evaluate())
}

class Tasks<T> where T: SQLTroupe, T: QueuePublishTroupe, T: SerializeTroupe {

}


class Skripter<T> where T: SQLTroupe, T: QueuePublishTroupe, T: SerializeTroupe {
    fun <I> of(): Skript<I, I, T> {
        return Skript.identity()
    }
}


interface Word<T> {
    abstract fun evaluate(): T
}

val one = Math.kNumber.One
val half = Math.kOperator.Half


sealed class Math: Word<Number> {

    infix fun <M: Math> plus(math: M): Math {
        return kOperator.Plus(this, math)
    }

    infix fun plus(number: Number): Math {
        return this.plus(number)
    }

    sealed class kOperator: Math() {
        data class Plus<M1: Math, M2: Math>(val left: M1, val right: M2): kOperator() {
            override fun evaluate(): Number {
                return left.evaluate().toDouble().plus(right.evaluate().toDouble())
            }
    }

        object Half {
            data class Of<M: Math>(val math: M): kOperator() {
                override fun evaluate(): Number {
                    return math.evaluate().toDouble() / 2
                }

            }

            infix fun <M: Math> of(math: M): Math = Of(math)
        }
    }

    sealed class kNumber : Math() {
        companion object {

        }

        data class Literal(val number: Number): kNumber() {
            override fun evaluate(): Number {
                return number
            }
        }

        object One: kNumber() {
            override fun evaluate(): Number {
                return 1
            }
        }
    }

}
