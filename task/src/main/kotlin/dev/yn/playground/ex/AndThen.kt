package dev.yn.playground.ex

import dev.yn.playground.Task

fun <I, O, O2, C: CP, CP> Task<I, O, C>.andThen(task: Task<O, O2, CP>): Task<I, O2, C> {
    return this.flatMap(Task.Wrapped<O, O2, C, CP>(task))
}