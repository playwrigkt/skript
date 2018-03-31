package playwrigkt.skript.serialize

import playwrigkt.skript.Skript
import playwrigkt.skript.performer.SerializeCommand
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stage.SerializeCast

sealed class SerializeSkript<I, O>: Skript<I, O, SerializeCast> {
    class Serialize<I>: SerializeSkript<I, ByteArray>() {
        override fun run(i: I, stage: SerializeCast): AsyncResult<ByteArray> {
            return stage.getSerializePerformer()
                    .serialize(SerializeCommand.Serialize(i))
        }
    }

    data class Deserialize<O>(val clazz: Class<O>): SerializeSkript<ByteArray, O>() {
        override fun run(i: ByteArray, stage: SerializeCast): AsyncResult<O> {
            return stage.getSerializePerformer()
                    .deserialize(SerializeCommand.Deserialize(i, clazz))
        }
    }
}