package playwright.skript.serialize

import playwright.skript.Skript
import playwright.skript.performer.SerializeCommand
import playwright.skript.result.AsyncResult
import playwright.skript.stage.SerializeStage

sealed class SerializeSkript<I, O>: Skript<I, O, SerializeStage<*>> {
    class Serialize<I>: SerializeSkript<I, ByteArray>() {
        override fun run(i: I, context: SerializeStage<*>): AsyncResult<ByteArray> {
            return context.getSerializePerformer()
                    .serialize(SerializeCommand.Serialize(i))
        }
    }

    data class Deserialize<O>(val clazz: Class<O>): SerializeSkript<ByteArray, O>() {
        override fun run(i: ByteArray, context: SerializeStage<*>): AsyncResult<O> {
            return context.getSerializePerformer()
                    .deserialize(SerializeCommand.Deserialize(i, clazz))
        }
    }
}