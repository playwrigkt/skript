package playwrigkt.skript.queue

import java.util.*

data class QueueMessage(val source: String, val body: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QueueMessage

        if (source != other.source) return false
        if (!Arrays.equals(body, other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + Arrays.hashCode(body)
        return result
    }
}
