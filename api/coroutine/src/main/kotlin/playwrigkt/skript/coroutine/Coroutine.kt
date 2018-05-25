package playwrigkt.skript.coroutine

import kotlinx.coroutines.experimental.launch
import arrow.core.Try
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult

data class CoroutineException(val error: CoroutineError,
                              override val cause: Throwable? = null): RuntimeException(error.toString(), cause)

sealed class CoroutineError {
    data class Timeout(val result: AsyncResult<*>): CoroutineError()
}

fun <T> runTryAsync(action: () -> Try<T>): AsyncResult<T> {
    val asyncResult = CompletableResult<T>()
    launch {
        action().fold(
                asyncResult::fail,
                asyncResult::succeed)
    }
    return asyncResult
}

fun <T> runAsync(action: () -> T): AsyncResult<T> {
    val asyncResult = CompletableResult<T>()
    launch {
        try {
            asyncResult.succeed(action())
        } catch(error: Throwable) {
            asyncResult.fail(error)
        }
    }
    return asyncResult
}

fun <T> runSuspended(action: suspend () -> Try<T>): AsyncResult<T> {
    val asyncResult = CompletableResult<T>()
    launch {
        action().fold(
                asyncResult::fail,
                asyncResult::succeed)
    }
    return asyncResult
}

