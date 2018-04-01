package playwrigkt.skript.ex

import playwrigkt.skript.Skript

fun <I, O, O2, Troupe, SubTroupe> Skript<I, O, Troupe>.andThen(skript: Skript<O, O2, SubTroupe>): Skript<I, O2, Troupe> where Troupe: SubTroupe {
    return this.flatMap(Skript.Wrapped<O, O2, Troupe, SubTroupe>(skript))
}