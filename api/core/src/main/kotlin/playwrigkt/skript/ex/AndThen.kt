package playwrigkt.skript.ex

import playwrigkt.skript.Skript

/**
 * Chain a skript with a Troupe that  subclasses this skript's Troupe
 */
fun <I, O, O2, Troupe, SubTroupe> Skript<I, O, Troupe>.andThen(skript: Skript<O, O2, SubTroupe>): Skript<I, O2, Troupe> where Troupe: SubTroupe {
    return this.compose(Skript.Wrapped<O, O2, Troupe, SubTroupe>(skript))
}