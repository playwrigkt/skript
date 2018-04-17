package playwrigkt.skript.vertx.ex

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.MultiMap
import playwrigkt.skript.result.CompletableResult

fun MultiMap.toMap(): Map<String, List<String>> = this.names().map { it to this.getAll(it)}.toMap()

fun <T> CompletableResult<T>.vertxHandler(): Handler<AsyncResult<T>> {
    val result = this
    return object : Handler<AsyncResult<T>> {
        override fun handle(event: AsyncResult<T>) {
            event.map(result::succeed)
                    .otherwise(result::fail)
        }
    }
}