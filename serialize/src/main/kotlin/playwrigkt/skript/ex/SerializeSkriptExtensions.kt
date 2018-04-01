package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.serialize.SerializeSkript
import playwrigkt.skript.troupe.SerializeTroupe


fun <I, O, Troupe> Skript<I, O, Troupe>.serialize(): Skript<I, ByteArray, Troupe> where Troupe: SerializeTroupe =
        this.andThen(SerializeSkript.Serialize())

fun <I, O, Troupe> Skript<I, ByteArray, Troupe>.deserialize(clazz: Class<O>): Skript<I, O, Troupe> where Troupe: SerializeTroupe =
        this.andThen(SerializeSkript.Deserialize(clazz))