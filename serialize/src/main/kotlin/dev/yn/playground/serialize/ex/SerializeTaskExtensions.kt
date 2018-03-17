package dev.yn.playground.serialize.ex

import dev.yn.playground.serialize.SerializeTask
import dev.yn.playground.serialize.SerializeTaskContext
import dev.yn.playground.task.Task
import dev.yn.playground.task.andThen


fun <I, O, C: SerializeTaskContext<*>> Task<I, O, C>.serialize(): Task<I, ByteArray, C> =
        this.andThen(SerializeTask.Serialize())

fun <I, O, C: SerializeTaskContext<*>> Task<I, ByteArray, C>.deserialize(clazz: Class<O>): Task<I, O, C> =
        this.andThen(SerializeTask.Deserialize(clazz))