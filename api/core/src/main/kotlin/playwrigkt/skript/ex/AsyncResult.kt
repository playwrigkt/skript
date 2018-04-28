package playwrigkt.skript.ex

import org.funktionale.tries.Try
import playwrigkt.skript.result.AsyncResult

/**
 * Turn a list of AsyncResults into an AsyncResult of a list
 */
fun <T> List<AsyncResult<out T>>.lift(): AsyncResult<List<T>> {
    //TODO refactor to aggregate errors
    return this
            .fold(AsyncResult.succeeded(emptyList<T>()))
            { results: AsyncResult<List<T>>, next: AsyncResult<out T> ->
                next.flatMap { t ->
                    results.map { it.plus(t) }
                }
            }
}

/**
* Turn a map of AsyncResults into an AsyncResult of a map
*/
fun <K, V> Map<K, AsyncResult<V>>.lift(): AsyncResult<Map<K, V>> {
    //TODO refactor to aggregate errors
    return this.entries
            .fold(AsyncResult.succeeded(emptyMap()))
            { results: AsyncResult<Map<K, V>>, next: Map.Entry<K, AsyncResult<out V>> ->
                next.value.flatMap { t ->
                    results.map { it.plus(next.key to t) }
                }
            }
}

/**
 * Convert a try to an asyncResult synchronously
 */
fun <T> Try<T>.toAsyncResult(): AsyncResult<T> =
        when(this) {
            is Try.Success -> AsyncResult.succeeded(this.get())
            is Try.Failure -> AsyncResult.failed(this.throwable)
        }