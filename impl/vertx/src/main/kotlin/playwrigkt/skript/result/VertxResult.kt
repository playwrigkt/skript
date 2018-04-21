package playwrigkt.skript.result

import io.vertx.core.Future

class VertxResult<T>(val future: Future<T>): AsyncResult<T> {
    override fun onSuccess(f: (T) -> Unit): AsyncResult<T> {
        future.setHandler {
            when {
                it.succeeded() -> f(it.result())
                else -> {}
            }
        }
        return this
    }

    override fun onFailure(f: (Throwable) -> Unit): AsyncResult<T> {
        future.setHandler {
            when {
                it.succeeded() -> {}
                else -> f(it.cause())
            }
        }
        return this
    }

    override fun addHandler(handler: (Result<T>) -> Unit) {
        future.setHandler {
            when {
                it.succeeded() -> handler(Result.Success(it.result()))
                else -> handler(Result.Failure(it.cause()))
            }
        }
    }

    override fun <U> map(f: (T) -> U): AsyncResult<U> {
        return VertxResult(future.map(f))
    }

    override fun <U> flatMap(f: (T) -> AsyncResult<U>): AsyncResult<U> {
        val result = CompletableResult<U>()
        future.setHandler {
            if(it.succeeded()) {
                f(it.result()).addHandler(result.completionHandler())
            } else {
                result.fail(it.cause())
            }
        }
        return result
    }

    override fun recover(f: (Throwable) -> AsyncResult<T>): AsyncResult<T> {
        val result = CompletableResult<T>()
        future.setHandler {
            if(it.succeeded()) {
                result.succeed(it.result())
            } else {
                f(it.cause()).addHandler(result.completionHandler())
            }
        }
        return result
    }

    override fun isComplete(): Boolean {
        return future.isComplete
    }

    override fun isSuccess(): Boolean {
        return future.succeeded()
    }

    override fun isFailure(): Boolean {
        return future.failed()
    }

    override fun result(): T? {
        return future.result()
    }

    override fun error(): Throwable? {
        return future.cause()
    }

}