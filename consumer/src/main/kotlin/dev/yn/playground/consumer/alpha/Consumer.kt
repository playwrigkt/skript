package dev.yn.playground.consumer.alpha

import dev.yn.playground.task.result.AsyncResult
import dev.yn.playground.task.result.Result

data class ConsumedMessage(val source: String, val body: ByteArray)

interface Consumer {
    fun isRunning(): Boolean
    fun stop(): AsyncResult<Unit>
    fun result(): AsyncResult<Unit>
}

interface Stream<T>: Consumer, Iterator<Result<T>>
interface Sink: Consumer