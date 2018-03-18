package playwright.skript.ex

import playwright.skript.Skript
import playwright.skript.serialize.SerializeSkript
import playwright.skript.stage.SerializeStage


fun <I, O, Stage: SerializeStage> Skript<I, O, Stage>.serialize(): Skript<I, ByteArray, Stage> =
        this.andThen(SerializeSkript.Serialize())

fun <I, O, Stage: SerializeStage> Skript<I, ByteArray, Stage>.deserialize(clazz: Class<O>): Skript<I, O, Stage> =
        this.andThen(SerializeSkript.Deserialize(clazz))