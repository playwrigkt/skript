package playwrigkt.skript.ex

import arrow.core.Try
import playwrigkt.skript.*

fun <I, OL, OR, O2, Troupe> Skript<I, Pair<OL, OR>, Troupe>.join(mapper: (OL, OR) -> O2): Skript<I, O2, Troupe> =
        this.map { mapper(it.first, it.second) }

fun <I, OL, OR, O2, Troupe> Skript<I, Pair<OL, OR>, Troupe>.joinTry(mapper: (OL, OR) -> Try<O2>): Skript<I, O2, Troupe> =
        this.mapTry { mapper(it.first, it.second) }

fun <I, O1, O2, O3, O, Troupe> Skript<I, Triple<O1, O2, O3>, Troupe>.join(mapper: (O1, O2, O3) -> O): Skript<I, O, Troupe> =
        this.map { mapper(it.first, it.second, it.third) }

fun <I, O1, O2, O3, O, Troupe> Skript<I, Triple<O1, O2, O3>, Troupe>.joinTry(mapper: (O1, O2, O3) -> Try<O>): Skript<I, O, Troupe> =
        this.mapTry { mapper(it.first, it.second, it.third) }

fun <I, O1, O2, O3, O4, O, Troupe> Skript<I, Quartet<O1, O2, O3, O4>, Troupe>.join(mapper: (O1, O2, O3, O4) -> O): Skript<I, O, Troupe> =
        this.map { mapper(it.one, it.two, it.three, it.four) }

fun <I, O1, O2, O3, O4, O, Troupe> Skript<I, Quartet<O1, O2, O3, O4>, Troupe>.joinTry(mapper: (O1, O2, O3, O4) -> Try<O>): Skript<I, O, Troupe> =
        this.mapTry { mapper(it.one, it.two, it.three, it.four) }

fun <I, O1, O2, O3, O4, O5, O, Troupe> Skript<I, Quintet<O1, O2, O3, O4, O5>, Troupe>.join(mapper: (O1, O2, O3, O4, O5) -> O): Skript<I, O, Troupe> =
        this.map { mapper(it.one, it.two, it.three, it.four, it.five) }

fun <I, O1, O2, O3, O4, O5, O, Troupe> Skript<I, Quintet<O1, O2, O3, O4, O5>, Troupe>.joinTry(mapper: (O1, O2, O3, O4, O5) -> Try<O>): Skript<I, O, Troupe> =
        this.mapTry { mapper(it.one, it.two, it.three, it.four, it.five) }

fun <I, O1, O2, O3, O4, O5, O6, O, Troupe> Skript<I, Sextet<O1, O2, O3, O4, O5, O6>, Troupe>.join(mapper: (O1, O2, O3, O4, O5, O6) -> O): Skript<I, O, Troupe> =
        this.map { mapper(it.one, it.two, it.three, it.four, it.five, it.six) }

fun <I, O1, O2, O3, O4, O5, O6, O, Troupe> Skript<I, Sextet<O1, O2, O3, O4, O5, O6>, Troupe>.joinTry(mapper: (O1, O2, O3, O4, O5, O6) -> Try<O>): Skript<I, O, Troupe> =
        this.mapTry { mapper(it.one, it.two, it.three, it.four, it.five, it.six) }

fun <I, O1, O2, O3, O4, O5, O6, O7, O, Troupe> Skript<I, Septet<O1, O2, O3, O4, O5, O6, O7>, Troupe>.join(mapper: (O1, O2, O3, O4, O5, O6, O7) -> O): Skript<I, O, Troupe> =
        this.map { mapper(it.one, it.two, it.three, it.four, it.five, it.six, it.seven) }

fun <I, O1, O2, O3, O4, O5, O6, O7, O, Troupe> Skript<I, Septet<O1, O2, O3, O4, O5, O6, O7>, Troupe>.joinTry(mapper: (O1, O2, O3, O4, O5, O6, O7) -> Try<O>): Skript<I, O, Troupe> =
        this.mapTry { mapper(it.one, it.two, it.three, it.four, it.five, it.six, it.seven) }

fun <I, O1, O2, O3, O4, O5, O6, O7, O8, O, Troupe> Skript<I, Octet<O1, O2, O3, O4, O5, O6, O7, O8>, Troupe>.join(mapper: (O1, O2, O3, O4, O5, O6, O7, O8) -> O): Skript<I, O, Troupe> =
        this.map { mapper(it.one, it.two, it.three, it.four, it.five, it.six, it.seven, it.eight) }

fun <I, O1, O2, O3, O4, O5, O6, O7, O8, O, Troupe> Skript<I, Octet<O1, O2, O3, O4, O5, O6, O7, O8>, Troupe>.joinTry(mapper: (O1, O2, O3, O4, O5, O6, O7, O8) -> Try<O>): Skript<I, O, Troupe> =
        this.mapTry { mapper(it.one, it.two, it.three, it.four, it.five, it.six, it.seven, it.eight) }

fun <I, O1, O2, O3, O4, O5, O6, O7, O8, O9, O, Troupe> Skript<I, Nonet<O1, O2, O3, O4, O5, O6, O7, O8, O9>, Troupe>.join(mapper: (O1, O2, O3, O4, O5, O6, O7, O8, O9) -> O): Skript<I, O, Troupe> =
        this.map { mapper(it.one, it.two, it.three, it.four, it.five, it.six, it.seven, it.eight, it.nine) }

fun <I, O1, O2, O3, O4, O5, O6, O7, O8, O9, O, Troupe> Skript<I, Nonet<O1, O2, O3, O4, O5, O6, O7, O8, O9>, Troupe>.joinTry(mapper: (O1, O2, O3, O4, O5, O6, O7, O8, O9) -> Try<O>): Skript<I, O, Troupe> =
        this.mapTry { mapper(it.one, it.two, it.three, it.four, it.five, it.six, it.seven, it.eight, it.nine) }

fun <I, O1, O2, O3, O4, O5, O6, O7, O8, O9, O10, O, Troupe> Skript<I, Dectet<O1, O2, O3, O4, O5, O6, O7, O8, O9, O10>, Troupe>.join(mapper: (O1, O2, O3, O4, O5, O6, O7, O8, O9, O10) -> O): Skript<I, O, Troupe> =
        this.map { mapper(it.one, it.two, it.three, it.four, it.five, it.six, it.seven, it.eight, it.nine, it.ten) }

fun <I, O1, O2, O3, O4, O5, O6, O7, O8, O9, O10, O, Troupe> Skript<I, Dectet<O1, O2, O3, O4, O5, O6, O7, O8, O9, O10>, Troupe>.joinTry(mapper: (O1, O2, O3, O4, O5, O6, O7, O8, O9, O10) -> Try<O>): Skript<I, O, Troupe> =
        this.mapTry { mapper(it.one, it.two, it.three, it.four, it.five, it.six, it.seven, it.eight, it.nine, it.ten) }
