package playwrigkt.skript.performer

import playwrigkt.skript.result.AsyncResult
import java.util.*

sealed class SerializeCommand<T> {
    data class Serialize<T>(val value: T): SerializeCommand<T>()
    data class Deserialize<T>(val bytes: ByteArray, val clazz: Class<T>): SerializeCommand<T>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Deserialize<*>

            if (!Arrays.equals(bytes, other.bytes)) return false
            if (clazz != other.clazz) return false

            return true
        }

        override fun hashCode(): Int {
            var result = Arrays.hashCode(bytes)
            result = 31 * result + clazz.hashCode()
            return result
        }
    }
}

interface SerializePerformer {
    fun <T> serialize(command: SerializeCommand.Serialize<T>): AsyncResult<ByteArray>
    fun <T> deserialize(command: SerializeCommand.Deserialize<T>): AsyncResult<T>
}

