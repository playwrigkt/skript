package dev.yn.playground.ex

import dev.yn.playground.Task
import dev.yn.playground.context.SerializeTaskContext
import dev.yn.playground.serialize.SerializeTask


fun <I, O, C: SerializeTaskContext<*>> Task<I, O, C>.serialize(): Task<I, ByteArray, C> =
        this.andThen(SerializeTask.Serialize())

fun <I, O, C: SerializeTaskContext<*>> Task<I, ByteArray, C>.deserialize(clazz: Class<O>): Task<I, O, C> =
        this.andThen(SerializeTask.Deserialize(clazz))