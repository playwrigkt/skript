package playwright.skript.ex

import playwright.skript.Skript
import playwright.skript.serialize.SerializeSkript
import playwright.skript.stage.SerializeCast


fun <I, O, Stage> Skript<I, O, Stage>.serialize(): Skript<I, ByteArray, Stage> where Stage: SerializeCast =
        this.andThen(SerializeSkript.Serialize())

fun <I, O, Stage> Skript<I, ByteArray, Stage>.deserialize(clazz: Class<O>): Skript<I, O, Stage> where Stage: SerializeCast =
        this.andThen(SerializeSkript.Deserialize(clazz))