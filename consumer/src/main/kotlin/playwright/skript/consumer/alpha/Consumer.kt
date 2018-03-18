package playwright.skript.consumer.alpha

import playwright.skript.result.AsyncResult
import playwright.skript.result.Result

data class ConsumedMessage(val source: String, val body: ByteArray)

interface Consumer {
    fun isRunning(): Boolean
    fun stop(): AsyncResult<Unit>
    fun result(): AsyncResult<Unit>
}

interface Stream<T>: playwright.skript.consumer.alpha.Consumer, Iterator<Result<T>>
interface Sink: playwright.skript.consumer.alpha.Consumer