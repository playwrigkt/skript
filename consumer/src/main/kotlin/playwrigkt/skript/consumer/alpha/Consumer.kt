package playwrigkt.skript.consumer.alpha

import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.Result

data class ConsumedMessage(val source: String, val body: ByteArray)

interface Consumer {
    fun isRunning(): Boolean
    fun stop(): AsyncResult<Unit>
    fun result(): AsyncResult<Unit>
}

interface Stream<T>: playwrigkt.skript.consumer.alpha.Consumer, Iterator<Result<T>>
interface Sink: playwrigkt.skript.consumer.alpha.Consumer