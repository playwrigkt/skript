package playwrigkt.skript.coroutine

import kotlinx.coroutines.experimental.launch
import org.funktionale.tries.Try
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult

data class CoroutineException(val error: CoroutineError,
                              override val cause: Throwable? = null): RuntimeException(error.toString(), cause)

sealed class CoroutineError {
    data class Timeout(val result: AsyncResult<*>): CoroutineError()
}

fun <T> runAsync(action: () -> Try<T>): AsyncResult<T> {
    val asyncResult = CompletableResult<T>()
    launch {
        action()
                .onSuccess(asyncResult::succeed)
                .onFailure(asyncResult::fail)
    }
    return asyncResult
}

fun <T> runSuspended(action: suspend () -> Try<T>): AsyncResult<T> {
    val asyncResult = CompletableResult<T>()
    launch {
        action()
                .onSuccess(asyncResult::succeed)
                .onFailure(asyncResult::fail)
    }
    return asyncResult
}

