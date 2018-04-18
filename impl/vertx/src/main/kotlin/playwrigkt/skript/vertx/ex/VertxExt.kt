package playwrigkt.skript.vertx.ex

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.MultiMap
import playwrigkt.skript.result.CompletableResult

fun MultiMap.toMap(): Map<String, List<String>> = this.names().map { it to this.getAll(it)}.toMap()

fun CompletableResult<Unit>.vertxHandler(): Handler<AsyncResult<Void>> {
    val result = this
    return object : Handler<AsyncResult<Void>> {
        override fun handle(event: AsyncResult<Void>) {
            event.map { result.succeed(Unit) }
                    .otherwise(result::fail)
        }
    }
}