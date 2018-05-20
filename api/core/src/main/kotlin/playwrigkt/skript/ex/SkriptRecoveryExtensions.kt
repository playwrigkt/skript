package playwrigkt.skript.ex

import playwrigkt.skript.Skript

fun <I, O, Troupe> Skript<I, O, Troupe>.recover(recovery: Skript<Pair<I, Throwable>, O, Troupe>): Skript<I, O, Troupe> =
        Skript.Recover(this, recovery)