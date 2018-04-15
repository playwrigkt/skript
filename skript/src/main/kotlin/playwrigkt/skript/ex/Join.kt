package playwrigkt.skript.ex

import org.funktionale.tries.Try
import playwrigkt.skript.Skript

fun <I, OL, OR, O2, Troupe> Skript<I, Pair<OL, OR>, Troupe>.join(mapper: (OL, OR) -> O2): Skript<I, O2, Troupe> =
        this.map { mapper(it.first, it.second) }
fun <I, OL, OR, O2, Troupe> Skript<I, Pair<OL, OR>, Troupe>.joinTry(mapper: (OL, OR) -> Try<O2>): Skript<I, O2, Troupe> =
        this.mapTry { mapper(it.first, it.second) }