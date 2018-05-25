package playwrigkt.skript.ex

import arrow.core.Try
import playwrigkt.skript.result.AsyncResult

/**
 * Turn a list of AsyncResults into an AsyncResult of a list
 */
fun <T> List<AsyncResult<out T>>.lift(): AsyncResult<List<T>> {
    return this
            .fold(AsyncResult.succeeded(Pair(emptyList(), emptyList())))
            { aggregate: AsyncResult<Pair<List<T>, List<Throwable>>>, next: AsyncResult<out T> ->
                next.also {result ->
                    aggregate.map {(succeeded, failed) ->
                        Pair(result.result?.let(succeeded::plus)?:succeeded, result.error?.let(failed::plus)?:failed)
                    }
                }
            }
            .flatMap {
                if(it.second.isNotEmpty()) {
                    AsyncResult.failed(AggregateException(it.second).flatten())
                } else {
                    AsyncResult.succeeded(it.first)
                }
            }
}

/**
* Turn a map of AsyncResults into an AsyncResult of a map
*/
fun <K, V> Map<K, AsyncResult<V>>.lift(): AsyncResult<Map<K, V>> {
    return this.entries
            .fold(AsyncResult.succeeded(Pair(emptyMap(), emptyList())))
            { aggregate: AsyncResult<Pair<Map<K, V>, List<Throwable>>>, next: Map.Entry<K, AsyncResult<out V>> ->
                next.value.also { result ->
                    aggregate.map { (succeeded, failed) ->
                        Pair(
                            result.result?.let { succeeded.plus(Pair(next.key, it))}?:succeeded,
                            result.error?.let(failed::plus)?:failed
                        )
                    }
                }
            }
            .flatMap {
                if(it.second.isNotEmpty()) {
                    AsyncResult.failed(AggregateException(it.second).flatten())
                } else {
                    AsyncResult.succeeded(it.first)
                }
            }
}

/**
 * Convert a try to an asyncResult synchronously
 */
fun <T> Try<T>.toAsyncResult(): AsyncResult<T> =
        when(this) {
            is Try.Success -> AsyncResult.succeeded(this.value)
            is Try.Failure -> AsyncResult.failed(this.exception)
        }