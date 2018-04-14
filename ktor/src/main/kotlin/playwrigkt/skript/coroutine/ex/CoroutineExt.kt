package playwrigkt.skript.coroutine.ex

import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult

fun <T, U> AsyncResult<T>.mapSuspend(fn: suspend (T) -> U): AsyncResult<U> =
    this.flatMap { t ->
        val newResult = CompletableResult<U>()
        suspend {
            try {
                newResult.succeed(fn(t))
            } catch (t: Throwable) {
                newResult.fail(t)
            }
        }
        newResult
    }