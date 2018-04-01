package playwrigkt.skript.serialize

import playwrigkt.skript.Skript
import playwrigkt.skript.performer.SerializeCommand
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SerializeTroupe

sealed class SerializeSkript<I, O>: Skript<I, O, SerializeTroupe> {
    class Serialize<I>: SerializeSkript<I, ByteArray>() {
        override fun run(i: I, troupe: SerializeTroupe): AsyncResult<ByteArray> {
            return troupe.getSerializePerformer()
                    .serialize(SerializeCommand.Serialize(i))
        }
    }

    data class Deserialize<O>(val clazz: Class<O>): SerializeSkript<ByteArray, O>() {
        override fun run(i: ByteArray, troupe: SerializeTroupe): AsyncResult<O> {
            return troupe.getSerializePerformer()
                    .deserialize(SerializeCommand.Deserialize(i, clazz))
        }
    }
}