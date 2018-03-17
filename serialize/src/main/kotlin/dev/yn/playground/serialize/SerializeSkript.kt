package dev.yn.playground.serialize

import dev.yn.playground.Skript
import dev.yn.playground.context.SerializeSkriptContext
import dev.yn.playground.result.AsyncResult

sealed class SerializeSkript<I, O>: Skript<I, O, SerializeSkriptContext<*>> {
    class Serialize<I>: SerializeSkript<I, ByteArray>() {
        override fun run(i: I, context: SerializeSkriptContext<*>): AsyncResult<ByteArray> {
            return context.getSerializeSkriptExecutor()
                    .serialize(SerializeCommand.Serialize(i))
        }
    }

    data class Deserialize<O>(val clazz: Class<O>): SerializeSkript<ByteArray, O>() {
        override fun run(i: ByteArray, context: SerializeSkriptContext<*>): AsyncResult<O> {
            return context.getSerializeSkriptExecutor()
                    .deserialize(SerializeCommand.Deserialize(i, clazz))
        }
    }
}