package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.serialize.SerializeSkript
import playwrigkt.skript.stage.SerializeStage


fun <I, O, Stage> Skript<I, O, Stage>.serialize(): Skript<I, ByteArray, Stage> where Stage: SerializeStage =
        this.andThen(SerializeSkript.Serialize())

fun <I, O, Stage> Skript<I, ByteArray, Stage>.deserialize(clazz: Class<O>): Skript<I, O, Stage> where Stage: SerializeStage =
        this.andThen(SerializeSkript.Deserialize(clazz))