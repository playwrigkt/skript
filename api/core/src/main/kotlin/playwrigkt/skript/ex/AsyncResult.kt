package playwrigkt.skript.ex

import playwrigkt.skript.result.AsyncResult

fun <T> List<AsyncResult<out T>>.lift(): AsyncResult<List<T>> {
    return this
            .fold(AsyncResult.succeeded(emptyList<T>()))
            { results: AsyncResult<List<T>>, next: AsyncResult<out T> ->
                next.flatMap { t ->
                    results.map { it.plus(t) }
                }
            }
}