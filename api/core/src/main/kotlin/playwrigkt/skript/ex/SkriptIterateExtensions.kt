package playwrigkt.skript.ex

import playwrigkt.skript.Skript

fun <I, O, O2, Troupe> Skript<I, List<O>, Troupe>.iterate(skript: Skript<O, O2, Troupe>): Skript<I, List<O2>, Troupe> =
        this.compose(Skript.SkriptIterate(skript))

fun <I, K, V, V2, Troupe> Skript<I, Map<K,V>, Troupe>.iterateValues(skript: Skript<V, V2, Troupe>): Skript<I, Map<K, V2>, Troupe> =
        this
                .map { it.toList() }
                .iterate(Skript.both(
                        Skript.identity<Pair<K, V>, Troupe>()
                                .map { it.first },
                        Skript.identity<Pair<K, V>, Troupe>()
                                .map { it.second }
                                .andThen(skript)))
                .map { it.toMap() }