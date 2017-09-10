package dev.yn.playground.util

import io.vertx.core.Future
import org.funktionale.tries.Try
object TryUtil {
    fun <R> handleFailure(tri: Try<R>): Future<R> =
            tri
                    .map { Future.succeededFuture(it) }
                    .getOrElse {
                        tri
                                .failed()
                                .map { Future.failedFuture<R>(it) }
                                .get()
                    }

    val unitSuccess: Try<Unit> = Try.Success(Unit)
}