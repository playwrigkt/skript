package playwrigkt.skript.coroutine.ex

import kotlinx.coroutines.experimental.launch
import org.funktionale.tries.Try
import playwrigkt.skript.coroutine.CoroutineError
import playwrigkt.skript.coroutine.CoroutineException
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import kotlin.math.min

fun <T, U> AsyncResult<T>.suspendMap(fn: suspend (T) -> U): AsyncResult<U> =
    this.flatMap { t ->
        val newResult = CompletableResult<U>()
        launch {
            try {
                newResult.succeed(fn(t))
            } catch (t: Throwable) {
                newResult.fail(t)
            }
        }
        newResult
    }

suspend fun <T> AsyncResult<T>.await(maxMillis: Long = 1000): Try<T> {
    val start = System.currentTimeMillis()
    while(!this.isComplete() && System.currentTimeMillis() - start < maxMillis) {
        launch { Thread.sleep(min(1000, maxMillis / 20)) }.join()
    }
    return this.result()?.let { Try.Success(it) }
            ?:this.error()?.let { Try.Failure<T>(it) }
            ?: Try.Failure(CoroutineException(CoroutineError.Timeout(this)))
}