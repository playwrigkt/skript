package playwright.skript.ex

import playwright.skript.Skript
import playwright.skript.serialize.SerializeSkript
import playwright.skript.stage.SerializeStage


fun <I, O, C: SerializeStage<*>> Skript<I, O, C>.serialize(): Skript<I, ByteArray, C> =
        this.andThen(SerializeSkript.Serialize())

fun <I, O, C: SerializeStage<*>> Skript<I, ByteArray, C>.deserialize(clazz: Class<O>): Skript<I, O, C> =
        this.andThen(SerializeSkript.Deserialize(clazz))