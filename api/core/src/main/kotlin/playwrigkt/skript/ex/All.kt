package playwrigkt.skript.ex

import playwrigkt.skript.*

fun <I, O, O1, O2, O3, Troupe> Skript<I, O, Troupe>.all(
        one: Skript<O, O1, Troupe>,
        two: Skript<O, O2, Troupe>,
        three: Skript<O, O3, Troupe>): Skript<I, Triple<O1, O2, O3>, Troupe> =
        this.compose(All3(one, two, three))

fun <I, O, O1, O2, O3, O4, Troupe> Skript<I, O, Troupe>.all(
        one: Skript<O, O1, Troupe>,
        two: Skript<O, O2, Troupe>,
        three: Skript<O, O3, Troupe>,
        four: Skript<O, O4, Troupe>): Skript<I, Quartet<O1, O2, O3, O4>, Troupe> =
        this.compose(All4(one, two, three, four))

fun <I, O, O1, O2, O3, O4, O5, Troupe> Skript<I, O, Troupe>.all(
        one: Skript<O, O1, Troupe>,
        two: Skript<O, O2, Troupe>,
        three: Skript<O, O3, Troupe>,
        four: Skript<O, O4, Troupe>,
        five: Skript<O, O5, Troupe>): Skript<I, Quintet<O1, O2, O3, O4, O5>, Troupe> =
        this.compose(All5(one, two, three, four, five))

fun <I, O, O1, O2, O3, O4, O5, O6, Troupe> Skript<I, O, Troupe>.all(
        one: Skript<O, O1, Troupe>,
        two: Skript<O, O2, Troupe>,
        three: Skript<O, O3, Troupe>,
        four: Skript<O, O4, Troupe>,
        five: Skript<O, O5, Troupe>,
        six: Skript<O, O6, Troupe>): Skript<I, Sextet<O1, O2, O3, O4, O5, O6>, Troupe> =
        this.compose(All6(one, two, three, four, five, six))

fun <I, O, O1, O2, O3, O4, O5, O6, O7, Troupe> Skript<I, O, Troupe>.all(
        one: Skript<O, O1, Troupe>,
        two: Skript<O, O2, Troupe>,
        three: Skript<O, O3, Troupe>,
        four: Skript<O, O4, Troupe>,
        five: Skript<O, O5, Troupe>,
        six: Skript<O, O6, Troupe>,
        seven: Skript<O, O7, Troupe>): Skript<I, Septet<O1, O2, O3, O4, O5, O6, O7>, Troupe> =
        this.compose(All7(one, two, three, four, five, six, seven))

fun <I, O, O1, O2, O3, O4, O5, O6, O7, O8, Troupe> Skript<I, O, Troupe>.all(
        one: Skript<O, O1, Troupe>,
        two: Skript<O, O2, Troupe>,
        three: Skript<O, O3, Troupe>,
        four: Skript<O, O4, Troupe>,
        five: Skript<O, O5, Troupe>,
        six: Skript<O, O6, Troupe>,
        seven: Skript<O, O7, Troupe>,
        eight: Skript<O, O8, Troupe>): Skript<I, Octet<O1, O2, O3, O4, O5, O6, O7, O8>, Troupe> =
        this.compose(All8(one, two, three, four, five, six, seven, eight))

fun <I, O, O1, O2, O3, O4, O5, O6, O7, O8, O9, Troupe> Skript<I, O, Troupe>.all(
        one: Skript<O, O1, Troupe>,
        two: Skript<O, O2, Troupe>,
        three: Skript<O, O3, Troupe>,
        four: Skript<O, O4, Troupe>,
        five: Skript<O, O5, Troupe>,
        six: Skript<O, O6, Troupe>,
        seven: Skript<O, O7, Troupe>,
        eight: Skript<O, O8, Troupe>,
        nine: Skript<O, O9, Troupe>): Skript<I, Nonet<O1, O2, O3, O4, O5, O6, O7, O8, O9>, Troupe> =
        this.compose(All9(one, two, three, four, five, six, seven, eight, nine))

fun <I, O, O1, O2, O3, O4, O5, O6, O7, O8, O9, O10, Troupe> Skript<I, O, Troupe>.all(
        one: Skript<O, O1, Troupe>,
        two: Skript<O, O2, Troupe>,
        three: Skript<O, O3, Troupe>,
        four: Skript<O, O4, Troupe>,
        five: Skript<O, O5, Troupe>,
        six: Skript<O, O6, Troupe>,
        seven: Skript<O, O7, Troupe>,
        eight: Skript<O, O8, Troupe>,
        nine: Skript<O, O9, Troupe>,
        ten: Skript<O, O10, Troupe>): Skript<I, Dectet<O1, O2, O3, O4, O5, O6, O7, O8, O9, O10>, Troupe> =
        this.compose(All10(one, two, three, four, five, six, seven, eight, nine, ten))