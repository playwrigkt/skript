package playwrigkt.skript.coroutine.ex

import kotlinx.coroutines.experimental.launch
import org.funktionale.tries.Try
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import kotlin.math.min

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

suspend fun <T> AsyncResult<T>.await(maxMillis: Long = 1000): Try<T> {
    val start = System.currentTimeMillis()
    while(!this.isComplete() && System.currentTimeMillis() - start < maxMillis) {
        launch { Thread.sleep(min(1000, maxMillis / 20)) }.join()
    }
    return this.result()?.let { Try.Success(it) }
            ?:this.error()?.let { Try.Failure<T>(it) }
            ?:Try.Failure(CoroutineException(CoroutineError.Timeout(this)))
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

