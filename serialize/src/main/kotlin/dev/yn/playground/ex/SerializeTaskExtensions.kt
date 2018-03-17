package dev.yn.playground.ex

import dev.yn.playground.Skript
import dev.yn.playground.context.SerializeTaskContext
import dev.yn.playground.serialize.SerializeSkript


fun <I, O, C: SerializeTaskContext<*>> Skript<I, O, C>.serialize(): Skript<I, ByteArray, C> =
        this.andThen(SerializeSkript.Serialize())

fun <I, O, C: SerializeTaskContext<*>> Skript<I, ByteArray, C>.deserialize(clazz: Class<O>): Skript<I, O, C> =
        this.andThen(SerializeSkript.Deserialize(clazz))