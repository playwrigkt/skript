package dev.yn.playground.serialize

import dev.yn.playground.Skript
import dev.yn.playground.context.SerializeTaskContext
import dev.yn.playground.result.AsyncResult

sealed class SerializeSkript<I, O>: Skript<I, O, SerializeTaskContext<*>> {
    class Serialize<I>: SerializeSkript<I, ByteArray>() {
        override fun run(i: I, context: SerializeTaskContext<*>): AsyncResult<ByteArray> {
            return context.getSerializeTaskExecutor()
                    .serialize(SerializeCommand.Serialize(i))
        }
    }

    data class Deserialize<O>(val clazz: Class<O>): SerializeSkript<ByteArray, O>() {
        override fun run(i: ByteArray, context: SerializeTaskContext<*>): AsyncResult<O> {
            return context.getSerializeTaskExecutor()
                    .deserialize(SerializeCommand.Deserialize(i, clazz))
        }
    }
}