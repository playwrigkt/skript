package playwrigkt.skript

import playwrigkt.skript.result.AsyncResult

/**
 * a tuple of 4 things
 */
data class Quartet<A, B, C, D>(val one: A, val two: B, val three: C, val four: D)

/**
 * a tuple of 5 things
 */
data class Quintet<A, B, C, D, E>(val one: A, val two: B, val three: C, val four: D, val five: E)

/**
 * a tuple of 6 things
 */
data class Sextet<A, B, C, D, E, F>(val one: A, val two: B, val three: C, val four: D, val five: E, val six: F)

/**
 * a tuple of 7 things
 */
data class Septet<A, B, C, D, E, F, G>(val one: A, val two: B, val three: C, val four: D, val five: E, val six: F, val seven: G)

/**
 * a tuple of 8 things
 */
data class Octet<A, B, C, D, E, F, G, H>(val one: A, val two: B, val three: C, val four: D, val five: E, val six: F, val seven: G, val eight: H)

/**
 * a tuple of 9 things
 */
data class Nonet<A, B, C, D, E, F, G, H, I>(val one: A, val two: B, val three: C, val four: D, val five: E, val six: F, val seven: G, val eight: H, val nine: I)

/**
 * a tuple of 10 things
 */
data class Dectet<A, B, C, D, E, F, G, H, I, J>(val one: A, val two: B, val three: C, val four: D, val five: E, val six: F, val seven: G, val eight: H, val nine: I, val ten: J)

/**
 * 3 parallel skripts
 */
data class All3<I, O1,O2, O3, Troupe>(
        val one: Skript<I, O1, Troupe>,
        val two: Skript<I, O2, Troupe>,
        val three: Skript<I, O3, Troupe>): Skript<I, Triple<O1, O2, O3>, Troupe> {
    override fun run(i: I, troupe: Troupe): AsyncResult<Triple<O1, O2, O3>> {
        val resultOne = one.run(i, troupe)
        val resultTwo = two.run(i, troupe)
        val resultThree = three.run(i, troupe)
        return resultOne.flatMap { first ->
            resultTwo.flatMap { second ->
            resultThree.map { third ->
                Triple(first, second, third)
            } } }
    }
}

/**
 * 4 parallel skripts
 */
data class All4<I, O1,O2, O3, O4, Troupe>(
        val one: Skript<I, O1, Troupe>,
        val two: Skript<I, O2, Troupe>,
        val three: Skript<I, O3, Troupe>,
        val four: Skript<I, O4, Troupe>): Skript<I, Quartet<O1, O2, O3, O4>, Troupe> {
    override fun run(i: I, troupe: Troupe): AsyncResult<Quartet<O1, O2, O3, O4>> {
        val resultOne = one.run(i, troupe)
        val resultTwo = two.run(i, troupe)
        val resultThree = three.run(i, troupe)
        val resultFour = four.run(i, troupe)
        return resultOne.flatMap { first ->
            resultTwo.flatMap { second ->
            resultThree.flatMap { third ->
            resultFour.map { fourth ->
                Quartet(first, second, third, fourth)
            } } } }
    }
}

/**
 * 5 parallel skripts
 */
data class All5<I, O1,O2, O3, O4, O5, Troupe>(
        val one: Skript<I, O1, Troupe>,
        val two: Skript<I, O2, Troupe>,
        val three: Skript<I, O3, Troupe>,
        val four: Skript<I, O4, Troupe>,
        val five: Skript<I, O5, Troupe>): Skript<I, Quintet<O1, O2, O3, O4, O5>, Troupe> {
    override fun run(i: I, troupe: Troupe): AsyncResult<Quintet<O1, O2, O3, O4, O5>> {
        val resultOne = one.run(i, troupe)
        val resultTwo = two.run(i, troupe)
        val resultThree = three.run(i, troupe)
        val resultFour = four.run(i, troupe)
        val resultFive = five.run(i, troupe)

        return resultOne.flatMap { first ->
            resultTwo.flatMap { second ->
            resultThree.flatMap { third ->
            resultFour.flatMap { fourth ->
            resultFive.map { fifth ->
                Quintet(first, second, third, fourth, fifth)
            } } } } }
    }
}

/**
 * 6 parallel skripts
 */
data class All6<I, O1,O2, O3, O4, O5, O6, Troupe>(
        val one: Skript<I, O1, Troupe>,
        val two: Skript<I, O2, Troupe>,
        val three: Skript<I, O3, Troupe>,
        val four: Skript<I, O4, Troupe>,
        val five: Skript<I, O5, Troupe>,
        val six: Skript<I, O6, Troupe>): Skript<I, Sextet<O1, O2, O3, O4, O5, O6>, Troupe> {
    override fun run(i: I, troupe: Troupe): AsyncResult<Sextet<O1, O2, O3, O4, O5, O6>> {
        val resultOne = one.run(i, troupe)
        val resultTwo = two.run(i, troupe)
        val resultThree = three.run(i, troupe)
        val resultFour = four.run(i, troupe)
        val resultFive = five.run(i, troupe)
        val resultSix = six.run(i, troupe)
        return resultOne.flatMap { first ->
            resultTwo.flatMap { second ->
            resultThree.flatMap { third ->
            resultFour.flatMap { fourth ->
            resultFive.flatMap { fifth ->
            resultSix.map { sixth ->
                Sextet(first, second, third, fourth, fifth, sixth)
            } } } } } }
    }
}

/**
 * 7 parallel skripts
 */
data class All7<I, O1,O2, O3, O4, O5, O6, O7, Troupe>(
        val one: Skript<I, O1, Troupe>,
        val two: Skript<I, O2, Troupe>,
        val three: Skript<I, O3, Troupe>,
        val four: Skript<I, O4, Troupe>,
        val five: Skript<I, O5, Troupe>,
        val six: Skript<I, O6, Troupe>,
        val seven: Skript<I, O7, Troupe>): Skript<I, Septet<O1, O2, O3, O4, O5, O6, O7>, Troupe> {
    override fun run(i: I, troupe: Troupe): AsyncResult<Septet<O1, O2, O3, O4, O5, O6, O7>> {
        val resultOne = one.run(i, troupe)
        val resultTwo = two.run(i, troupe)
        val resultThree = three.run(i, troupe)
        val resultFour = four.run(i, troupe)
        val resultFive = five.run(i, troupe)
        val resultSix = six.run(i, troupe)
        val resultSeven = seven.run(i, troupe)

        return resultOne.flatMap { first ->
            resultTwo.flatMap { second ->
            resultThree.flatMap { third ->
            resultFour.flatMap { fourth ->
            resultFive.flatMap { fifth ->
            resultSix.flatMap { sixth ->
            resultSeven.map { seventh ->
                Septet(first, second, third, fourth, fifth, sixth, seventh)
            } } } } } } }
    }
}

/**
 * 8 parallel skripts
 */
data class All8<I, O1,O2, O3, O4, O5, O6, O7, O8, Troupe>(
        val one: Skript<I, O1, Troupe>,
        val two: Skript<I, O2, Troupe>,
        val three: Skript<I, O3, Troupe>,
        val four: Skript<I, O4, Troupe>,
        val five: Skript<I, O5, Troupe>,
        val six: Skript<I, O6, Troupe>,
        val seven: Skript<I, O7, Troupe>,
        val eight: Skript<I, O8, Troupe>): Skript<I, Octet<O1, O2, O3, O4, O5, O6, O7, O8>, Troupe> {
    override fun run(i: I, troupe: Troupe): AsyncResult<Octet<O1, O2, O3, O4, O5, O6, O7, O8>> {
        val resultOne = one.run(i, troupe)
        val resultTwo = two.run(i, troupe)
        val resultThree = three.run(i, troupe)
        val resultFour = four.run(i, troupe)
        val resultFive = five.run(i, troupe)
        val resultSix = six.run(i, troupe)
        val resultSeven = seven.run(i, troupe)
        val resultEight = eight.run(i, troupe)

        return resultOne.flatMap { first ->
            resultTwo.flatMap { second ->
            resultThree.flatMap { third ->
            resultFour.flatMap { fourth ->
            resultFive.flatMap { fifth ->
            resultSix.flatMap { sixth ->
            resultSeven.flatMap { seventh ->
            resultEight.map {eighth ->
                Octet(first, second, third, fourth, fifth, sixth, seventh, eighth)
            } } } } } } } }
    }
}

/**
 * 9 parallel skripts
 */
data class All9<I, O1,O2, O3, O4, O5, O6, O7, O8, O9, Troupe>(
        val one: Skript<I, O1, Troupe>,
        val two: Skript<I, O2, Troupe>,
        val three: Skript<I, O3, Troupe>,
        val four: Skript<I, O4, Troupe>,
        val five: Skript<I, O5, Troupe>,
        val six: Skript<I, O6, Troupe>,
        val seven: Skript<I, O7, Troupe>,
        val eight: Skript<I, O8, Troupe>,
        val nine: Skript<I, O9, Troupe>): Skript<I, Nonet<O1, O2, O3, O4, O5, O6, O7, O8, O9>, Troupe> {
    override fun run(i: I, troupe: Troupe): AsyncResult<Nonet<O1, O2, O3, O4, O5, O6, O7, O8, O9>> {
        val resultOne = one.run(i, troupe)
        val resultTwo = two.run(i, troupe)
        val resultThree = three.run(i, troupe)
        val resultFour = four.run(i, troupe)
        val resultFive = five.run(i, troupe)
        val resultSix = six.run(i, troupe)
        val resultSeven = seven.run(i, troupe)
        val resultEight = eight.run(i, troupe)
        val resultNine = nine.run(i, troupe)

        return resultOne.flatMap { first ->
            resultTwo.flatMap { second ->
            resultThree.flatMap { third ->
            resultFour.flatMap { fourth ->
            resultFive.flatMap { fifth ->
            resultSix.flatMap { sixth ->
            resultSeven.flatMap { seventh ->
            resultEight.flatMap {eighth ->
            resultNine.map { nineth ->
                Nonet(first, second, third, fourth, fifth, sixth, seventh, eighth, nineth)
            } } } } } } } } }
    }
}

/**
 * 10 parallel skripts
 */
data class All10<I, O1,O2, O3, O4, O5, O6, O7, O8, O9, O10, Troupe>(
        val one: Skript<I, O1, Troupe>,
        val two: Skript<I, O2, Troupe>,
        val three: Skript<I, O3, Troupe>,
        val four: Skript<I, O4, Troupe>,
        val five: Skript<I, O5, Troupe>,
        val six: Skript<I, O6, Troupe>,
        val seven: Skript<I, O7, Troupe>,
        val eight: Skript<I, O8, Troupe>,
        val nine: Skript<I, O9, Troupe>,
        val ten: Skript<I, O10, Troupe>): Skript<I, Dectet<O1, O2, O3, O4, O5, O6, O7, O8, O9, O10>, Troupe> {
    override fun run(i: I, troupe: Troupe): AsyncResult<Dectet<O1, O2, O3, O4, O5, O6, O7, O8, O9, O10>> {
        val resultOne = one.run(i, troupe)
        val resultTwo = two.run(i, troupe)
        val resultThree = three.run(i, troupe)
        val resultFour = four.run(i, troupe)
        val resultFive = five.run(i, troupe)
        val resultSix = six.run(i, troupe)
        val resultSeven = seven.run(i, troupe)
        val resultEight = eight.run(i, troupe)
        val resultNine = nine.run(i, troupe)
        val resultTen = ten.run(i, troupe)

        return resultOne.flatMap { first ->
            resultTwo.flatMap { second ->
            resultThree.flatMap { third ->
            resultFour.flatMap { fourth ->
            resultFive.flatMap { fifth ->
            resultSix.flatMap { sixth ->
            resultSeven.flatMap { seventh ->
            resultEight.flatMap {eighth ->
            resultNine.flatMap { nineth ->
            resultTen.map { tenth ->
                Dectet(first, second, third, fourth, fifth, sixth, seventh, eighth, nineth, tenth)
            } } } } } } } } } }
    }
}
