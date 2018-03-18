package playwright.skript.performer

import playwright.skript.result.AsyncResult

sealed class SerializeCommand<T> {
    data class Serialize<T>(val value: T): SerializeCommand<T>()
    data class Deserialize<T>(val bytes: ByteArray, val clazz: Class<T>): SerializeCommand<T>()
}

interface SerializePerformer {
    fun <T> serialize(command: SerializeCommand.Serialize<T>): AsyncResult<ByteArray>
    fun <T> deserialize(command: SerializeCommand.Deserialize<T>): AsyncResult<T>
}

