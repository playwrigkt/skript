package dev.yn.playground.util

import io.vertx.core.Future
import org.funktionale.tries.Try

object TryUtil {
    fun <R> handleFailure(tri: Try<R>): Future<R> =
        when(tri) {
            is Try.Failure -> Future.failedFuture(tri.throwable)
            is Try.Success -> Future.succeededFuture(tri.get())
        }
}