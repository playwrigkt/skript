package dev.yn.playground.ex

import dev.yn.playground.Skript

fun <I, O, O2, C: CP, CP> Skript<I, O, C>.andThen(skript: Skript<O, O2, CP>): Skript<I, O2, C> {
    return this.flatMap(Skript.Wrapped<O, O2, C, CP>(skript))
}