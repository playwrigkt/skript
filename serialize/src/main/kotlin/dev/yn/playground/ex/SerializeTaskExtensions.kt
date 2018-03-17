package dev.yn.playground.ex

import dev.yn.playground.Skript
import dev.yn.playground.context.SerializeSkriptContext
import dev.yn.playground.serialize.SerializeSkript


fun <I, O, C: SerializeSkriptContext<*>> Skript<I, O, C>.serialize(): Skript<I, ByteArray, C> =
        this.andThen(SerializeSkript.Serialize())

fun <I, O, C: SerializeSkriptContext<*>> Skript<I, ByteArray, C>.deserialize(clazz: Class<O>): Skript<I, O, C> =
        this.andThen(SerializeSkript.Deserialize(clazz))